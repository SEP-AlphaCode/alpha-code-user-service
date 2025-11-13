package com.alpha_code.alpha_code_user_service.service.impl;

import com.alpha_code.alpha_code_user_service.dto.NotificationDto;
import com.alpha_code.alpha_code_user_service.dto.PagedResult;
import com.alpha_code.alpha_code_user_service.enums.NotificationTypeEnum;
import com.alpha_code.alpha_code_user_service.exception.ResourceNotFoundException;
import com.alpha_code.alpha_code_user_service.publisher.NotificationPublisher;
import com.alpha_code.alpha_code_user_service.repository.AccountRepository;
import com.alpha_code.alpha_code_user_service.service.MailService;
import com.alpha_code.alpha_code_user_service.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final StringRedisTemplate redisTemplate;
    private final NotificationPublisher notificationPublisher;
    private final AccountRepository accountRepository;
    private final MailService mailService;
    private final ObjectMapper objectMapper;

    private static final String PREFIX = "notification:";
    private static final String ACCOUNT_SET_PREFIX = "notifications:account:";
    private static final String GLOBAL_SET = "notifications:all";

    private String notificationKey(UUID id) {
        return PREFIX + id.toString();
    }

    private String accountSetKey(UUID accountId) {
        return ACCOUNT_SET_PREFIX + accountId.toString();
    }

    // ---------------- Helper: adjust timezone +7h ----------------
    private NotificationDto adjustTimezone(NotificationDto dto) {
        if (dto.getCreatedDate() != null)
            dto.setCreatedDate(dto.getCreatedDate().plusHours(7));
        if (dto.getLastUpdated() != null)
            dto.setLastUpdated(dto.getLastUpdated().plusHours(7));
        return dto;
    }

    private List<NotificationDto> adjustTimezoneList(List<NotificationDto> dtos) {
        return dtos.stream().map(this::adjustTimezone).collect(Collectors.toList());
    }

    // ---------------- GET ALL ----------------
    @Override
    @Cacheable(value = "notifications_list", key = "{#page, #size, #accountId, #status}")
    public PagedResult<NotificationDto> getAll(int page, int size, UUID accountId, Integer status) {
        int pageIndex = Math.max(page - 1, 0);
        int start = pageIndex * size;
        int end = start + size - 1;

        String zKey = accountId != null ? accountSetKey(accountId) : GLOBAL_SET;
        Long total = redisTemplate.opsForZSet().zCard(zKey);
        if (total == null || total == 0) {
            return new PagedResult<>(new PageImpl<>(Collections.emptyList(), PageRequest.of(pageIndex, size), 0));
        }

        Set<String> idMembers = redisTemplate.opsForZSet().reverseRange(zKey, start, end);
        if (idMembers == null || idMembers.isEmpty()) {
            return new PagedResult<>(new PageImpl<>(Collections.emptyList(), PageRequest.of(pageIndex, size), total));
        }

        List<String> keys = idMembers.stream()
                .map(UUID::fromString)       // String -> UUID
                .map(this::notificationKey)  // UUID -> Redis key
                .collect(Collectors.toList());
        List<String> jsons = redisTemplate.opsForValue().multiGet(keys);
        List<NotificationDto> dtos = new ArrayList<>();
        if (jsons != null) {
            for (String j : jsons) {
                if (j == null) continue;
                try {
                    NotificationDto dto = objectMapper.readValue(j, NotificationDto.class);
                    if (status == null || Objects.equals(dto.getStatus(), status)) {
                        dtos.add(dto);
                    }
                } catch (Exception e) {
                    log.error("Failed to deserialize notification from Redis", e);
                }
            }
        }

        // Adjust timezone +7h
        dtos = adjustTimezoneList(dtos);

        Page<NotificationDto> pageResult = new PageImpl<>(dtos, PageRequest.of(pageIndex, size), total);
        return new PagedResult<>(pageResult);
    }

    // ---------------- GET BY ID ----------------
    @Override
    @Cacheable(value = "notifications", key = "#id")
    public NotificationDto getById(UUID id) {
        String key = notificationKey(id);
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) throw new ResourceNotFoundException("Notification not found");
        try {
            NotificationDto dto = objectMapper.readValue(json, NotificationDto.class);
            return adjustTimezone(dto);
        } catch (Exception e) {
            log.error("Deserialize error", e);
            throw new ResourceNotFoundException("Notification not found");
        }
    }

    // ---------------- CREATE ----------------
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "notifications_list", allEntries = true)
    })
    public NotificationDto create(NotificationDto notificationDto) {
        UUID id = notificationDto.getId() != null ? notificationDto.getId() : UUID.randomUUID();
        NotificationDto result = new NotificationDto();
        result.setId(id);
        result.setAccountId(notificationDto.getAccountId());
        result.setType(notificationDto.getType());
        result.setTitle(notificationDto.getTitle());
        result.setMessage(notificationDto.getMessage());
        result.setIsRead(notificationDto.getIsRead() != null ? notificationDto.getIsRead() : Boolean.FALSE);
        result.setStatus(notificationDto.getStatus());

        // Lưu UTC trong Redis
        Instant now = Instant.now();
        result.setCreatedDate(LocalDateTime.ofInstant(now, ZoneOffset.UTC));
        result.setLastUpdated(LocalDateTime.ofInstant(now, ZoneOffset.UTC));
        result.setOrderCode(notificationDto.getOrderCode());
        result.setServiceName(notificationDto.getServiceName());
        result.setPrice(notificationDto.getPrice());

        try {
            String json = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(notificationKey(id), json);

            long score = now.toEpochMilli();
            if (result.getAccountId() != null) {
                redisTemplate.opsForZSet().add(accountSetKey(result.getAccountId()), id.toString(), score);
            }
            redisTemplate.opsForZSet().add(GLOBAL_SET, id.toString(), score);

            // Realtime publish
            notificationPublisher.sendToUser(result.getAccountId(), result);

            // Send email if needed
            NotificationTypeEnum type = NotificationTypeEnum.fromCodeValue(result.getType());
            if (type == NotificationTypeEnum.PAYMENT_SUCCESS) {
                var account = accountRepository.findById(result.getAccountId())
                        .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
                mailService.sendPaymentSuccessEmail(account.getEmail(), account.getFullName(),
                        result.getServiceName(), result.getOrderCode(), result.getPrice());
            }
            if (type == NotificationTypeEnum.FINISHCOURSE) {
                var account = accountRepository.findById(result.getAccountId())
                        .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
                mailService.sendCourseCompletedEmail(account.getEmail(), account.getFullName(),
                        result.getMessage(), result.getServiceName(), result.getAccountId().toString());
            }
        } catch (Exception e) {
            log.error("Failed to save notification to Redis", e);
            throw new RuntimeException("Failed to save notification");
        }

        return adjustTimezone(result);
    }

    // ---------------- UPDATE ----------------
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "notifications_list", allEntries = true),
            @CacheEvict(value = "notifications", key = "#id")
    })
    public NotificationDto update(UUID id, NotificationDto notificationDto) {
        String key = notificationKey(id);
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) throw new ResourceNotFoundException("Notification not found");
        try {
            NotificationDto existing = objectMapper.readValue(json, NotificationDto.class);
            UUID oldAccountId = existing.getAccountId();

            existing.setAccountId(notificationDto.getAccountId());
            existing.setType(notificationDto.getType());
            existing.setTitle(notificationDto.getTitle());
            existing.setMessage(notificationDto.getMessage());
            existing.setIsRead(notificationDto.getIsRead());
            existing.setStatus(notificationDto.getStatus());
            existing.setLastUpdated(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC));

            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(existing));

            if (!Objects.equals(oldAccountId, notificationDto.getAccountId())) {
                if (oldAccountId != null)
                    redisTemplate.opsForZSet().remove(accountSetKey(oldAccountId), id.toString());
                if (notificationDto.getAccountId() != null)
                    redisTemplate.opsForZSet().add(accountSetKey(notificationDto.getAccountId()), id.toString(),
                            Instant.now().toEpochMilli());
            }

            return adjustTimezone(existing);
        } catch (Exception e) {
            log.error("Failed to update notification in Redis", e);
            throw new RuntimeException("Failed to update notification");
        }
    }

    // ---------------- PATCH UPDATE ----------------
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "notifications_list", allEntries = true),
            @CacheEvict(value = "notifications", key = "#id")
    })
    public NotificationDto patchUpdate(UUID id, NotificationDto notificationDto) {
        String key = notificationKey(id);
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) throw new ResourceNotFoundException("Notification not found");
        try {
            NotificationDto existing = objectMapper.readValue(json, NotificationDto.class);

            if (notificationDto.getAccountId() != null) existing.setAccountId(notificationDto.getAccountId());
            if (notificationDto.getType() != null) existing.setType(notificationDto.getType());
            if (notificationDto.getTitle() != null) existing.setTitle(notificationDto.getTitle());
            if (notificationDto.getMessage() != null) existing.setMessage(notificationDto.getMessage());
            if (notificationDto.getIsRead() != null) existing.setIsRead(notificationDto.getIsRead());
            if (notificationDto.getStatus() != null) existing.setStatus(notificationDto.getStatus());

            existing.setLastUpdated(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC));
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(existing));

            return adjustTimezone(existing);
        } catch (Exception e) {
            log.error("Failed to patch update notification in Redis", e);
            throw new RuntimeException("Failed to patch update notification");
        }
    }

    // ---------------- DELETE ----------------
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "notifications_list", allEntries = true),
            @CacheEvict(value = "notifications", key = "#id")
    })
    public String delete(UUID id) {
        String key = notificationKey(id);
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) throw new ResourceNotFoundException("Notification not found");
        try {
            NotificationDto existing = objectMapper.readValue(json, NotificationDto.class);
            redisTemplate.delete(key);
            if (existing.getAccountId() != null)
                redisTemplate.opsForZSet().remove(accountSetKey(existing.getAccountId()), id.toString());
            redisTemplate.opsForZSet().remove(GLOBAL_SET, id.toString());
            return "Deleted notification " + id;
        } catch (Exception e) {
            log.error("Failed to delete notification", e);
            throw new RuntimeException("Failed to delete notification");
        }
    }

    // ---------------- CHANGE STATUS ----------------
    @Override
    @Transactional
    @CacheEvict(value = "notifications", key = "#id")
    @Caching(evict = {
            @CacheEvict(value = "notifications_list", allEntries = true),
    })
    public NotificationDto changeStatus(UUID id, Integer status) {
        String key = notificationKey(id);
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) throw new ResourceNotFoundException("Notification not found");
        try {
            NotificationDto existing = objectMapper.readValue(json, NotificationDto.class);
            existing.setStatus(status);
            existing.setLastUpdated(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC));
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(existing));
            return adjustTimezone(existing);
        } catch (Exception e) {
            log.error("Failed to change status", e);
            throw new RuntimeException("Failed to change status");
        }
    }

    // ---------------- READ NOTIFICATION ----------------
    @Override
    @Transactional
    @CachePut(value = "notifications", key = "#id")
    @Caching(evict = {
            @CacheEvict(value = "notifications_list", allEntries = true),
    })
    public NotificationDto readNotification(UUID id) {
        String key = notificationKey(id);
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) throw new ResourceNotFoundException("Notification not found");
        try {
            NotificationDto existing = objectMapper.readValue(json, NotificationDto.class);
            existing.setIsRead(true);
            existing.setLastUpdated(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC));
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(existing));
            return adjustTimezone(existing);
        } catch (Exception e) {
            log.error("Failed to mark notification read", e);
            throw new RuntimeException("Failed to read notification");
        }
    }

    // ---------------- READ ALL NOTIFICATIONS ----------------
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "notifications_list", allEntries = true),
    })
    public Map<String, Object> readAllNotifications(UUID accountId) {
        if (accountId == null) throw new IllegalArgumentException("Account ID required");

        String zKey = accountSetKey(accountId);
        Set<String> allIds = redisTemplate.opsForZSet().range(zKey, 0, -1);
        if (allIds == null || allIds.isEmpty()) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "No notifications found");
            resp.put("count", 0);
            return resp;
        }

        int count = 0;
        for (String idStr : allIds) {
            try {
                UUID id = UUID.fromString(idStr);
                String key = notificationKey(id);
                String json = redisTemplate.opsForValue().get(key);
                if (json != null) {
                    NotificationDto dto = objectMapper.readValue(json, NotificationDto.class);
                    if (!Boolean.TRUE.equals(dto.getIsRead())) {
                        dto.setIsRead(true);
                        dto.setLastUpdated(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC));
                        redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(dto));
                        count++;
                    }
                }
            } catch (Exception e) {
                log.error("Failed to mark notification read: " + idStr, e);
            }
        }

        // Evict cache list related
        redisTemplate.delete(redisTemplate.keys("notifications_list*"));

        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "Successfully marked " + count + " notification(s) as read");
        resp.put("count", count);
        return resp;
    }
}
