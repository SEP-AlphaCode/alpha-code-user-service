package com.alpha_code.alpha_code_user_service.service.impl;

import com.alpha_code.alpha_code_user_service.dto.NotificationDto;
import com.alpha_code.alpha_code_user_service.dto.PagedResult;
import com.alpha_code.alpha_code_user_service.enums.NotificationTypeEnum;
import com.alpha_code.alpha_code_user_service.exception.ResourceNotFoundException;
import com.alpha_code.alpha_code_user_service.mapper.NotificationMapper;
import com.alpha_code.alpha_code_user_service.publisher.NotificationPublisher;
import com.alpha_code.alpha_code_user_service.repository.AccountRepository;
import com.alpha_code.alpha_code_user_service.service.MailService;
import com.alpha_code.alpha_code_user_service.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

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

    @Override
    @Cacheable(value = "notifications_list", key = "{#page, #size, #accountId, #status}")
    public PagedResult<NotificationDto> getAll(int page, int size, UUID accountId, Integer status) {
        // Use Redis sorted sets for pagination. We sort by created timestamp (stored as score).
        int pageIndex = Math.max(page - 1, 0);
        int start = pageIndex * size;
        int end = start + size - 1;

        String zKey = (accountId != null) ? accountSetKey(accountId) : GLOBAL_SET;

        Long total = redisTemplate.opsForZSet().zCard(zKey);
        if (total == null || total == 0) {
            Page<NotificationDto> empty = new PageImpl<>(Collections.emptyList(), PageRequest.of(pageIndex, size), 0);
            return new PagedResult<>(empty);
        }

        Set<String> idMembers = redisTemplate.opsForZSet().reverseRange(zKey, start, end);
        if (idMembers == null || idMembers.isEmpty()) {
            Page<NotificationDto> empty = new PageImpl<>(Collections.emptyList(), PageRequest.of(pageIndex, size), total);
            return new PagedResult<>(empty);
        }

        List<String> keys = idMembers.stream().map(s -> PREFIX + s).collect(Collectors.toList());
        List<String> jsons = redisTemplate.opsForValue().multiGet(keys);
        List<NotificationDto> dtos = new ArrayList<>();
        if (jsons != null) {
            for (String j : jsons) {
                if (j == null) continue;
                try {
                    NotificationDto dto = objectMapper.readValue(j, NotificationDto.class);
                    // Filter by status if requested
                    if (status == null || Objects.equals(dto.getStatus(), status)) {
                        dtos.add(dto);
                    }
                } catch (Exception e) {
                    log.error("Failed to deserialize notification from Redis", e);
                }
            }
        }

        Page<NotificationDto> pageResult = new PageImpl<>(dtos, PageRequest.of(pageIndex, size), total);
        return new PagedResult<>(pageResult);
    }

    @Override
    @Cacheable(value = "notifications", key = "#id")
    public NotificationDto getById(UUID id) {
        String key = notificationKey(id);
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            throw new ResourceNotFoundException("Notification not found");
        }
        try {
            return objectMapper.readValue(json, NotificationDto.class);
        } catch (Exception e) {
            log.error("Deserialize error", e);
            throw new ResourceNotFoundException("Notification not found");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"notifications_list", "notifications"}, allEntries = true)
    public NotificationDto create(NotificationDto notificationDto) {
        // Prepare DTO
        NotificationDto result = new NotificationDto();
        UUID id = notificationDto.getId() != null ? notificationDto.getId() : UUID.randomUUID();
        result.setId(id);

        Instant now = Instant.now();
        result.setCreatedDate(LocalDateTime.now());
        result.setAccountId(notificationDto.getAccountId());
        result.setType(notificationDto.getType());
        result.setTitle(notificationDto.getTitle());
        result.setMessage(notificationDto.getMessage());
        result.setIsRead(notificationDto.getIsRead() != null ? notificationDto.getIsRead() : Boolean.FALSE);
        result.setStatus(notificationDto.getStatus());
        result.setLastUpdated(LocalDateTime.now());
        result.setOrderCode(notificationDto.getOrderCode());
        result.setServiceName(notificationDto.getServiceName());
        result.setPrice(notificationDto.getPrice());

        try {
            String json = objectMapper.writeValueAsString(result);
            String key = notificationKey(id);
            // Save JSON
            redisTemplate.opsForValue().set(key, json);
            // Add to account sorted set and global set with score = epoch millis
            long score = now.toEpochMilli();
            if (result.getAccountId() != null) {
                redisTemplate.opsForZSet().add(accountSetKey(result.getAccountId()), id.toString(), score);
            }
            redisTemplate.opsForZSet().add(GLOBAL_SET, id.toString(), score);
        } catch (Exception e) {
            log.error("Failed to save notification to Redis", e);
            throw new RuntimeException("Failed to save notification");
        }

        // Send realtime socket - ✅ FIX: send 'result' thay vì 'notificationDto'
        notificationPublisher.sendToUser(result.getAccountId(), result);

        try {
            NotificationTypeEnum type = NotificationTypeEnum.fromCodeValue(result.getType());

            if (type == NotificationTypeEnum.PAYMENT_SUCCESS) {
                var account = accountRepository.findById(result.getAccountId())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản để gửi email"));

                mailService.sendPaymentSuccessEmail(
                        account.getEmail(),
                        account.getFullName(),
                        notificationDto.getServiceName(),  // Tên gói dịch vụ
                        notificationDto.getOrderCode(),    // Mã đơn hàng
                        notificationDto.getPrice()         // Giá
                );
            }

            if(type == NotificationTypeEnum.FINISHCOURSE){
                var account = accountRepository.findById(result.getAccountId())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản để gửi email"));

                mailService.sendCourseCompletedEmail(
                        account.getEmail(),
                        account.getFullName(),
                        notificationDto.getServiceName(),
                        notificationDto.getMessage(), // Tên khóa học
                        notificationDto.getAccountId().toString()
                );
            }

        } catch (Exception e) {
            log.error("Lỗi khi gửi email thông báo: ", e);
        }

        return result;
    }

    @Override
    @Transactional
    @CacheEvict(value = "notifications_list", allEntries = true)
    @CachePut(value = "notifications", key = "#notificationDto.id")
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
            existing.setLastUpdated(LocalDateTime.now());

            String newJson = objectMapper.writeValueAsString(existing);
            redisTemplate.opsForValue().set(key, newJson);
            // ensure sorted sets updated if accountId changed
            if (!Objects.equals(oldAccountId, notificationDto.getAccountId())) {
                // remove from old account set and add to new one
                // best-effort: remove from all account sets is expensive; assume accountId provided correctly
                if (oldAccountId != null) {
                    redisTemplate.opsForZSet().remove(accountSetKey(oldAccountId), id.toString());
                }
                if (notificationDto.getAccountId() != null) {
                    redisTemplate.opsForZSet().add(accountSetKey(notificationDto.getAccountId()), id.toString(), Instant.now().toEpochMilli());
                }
            }
            return existing;
        } catch (Exception e) {
            log.error("Failed to update notification in Redis", e);
            throw new RuntimeException("Failed to update notification");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "notifications_list", allEntries = true)
    @CachePut(value = "notifications", key = "#notificationDto.id")
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

            existing.setLastUpdated(LocalDateTime.now());
            String newJson = objectMapper.writeValueAsString(existing);
            redisTemplate.opsForValue().set(key, newJson);
            return existing;
        } catch (Exception e) {
            log.error("Failed to patch update notification in Redis", e);
            throw new RuntimeException("Failed to patch update notification");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "{notifications_list, notifications}", allEntries = true)
    public String delete(UUID id) {
        String key = notificationKey(id);
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) throw new ResourceNotFoundException("Notification not found");
        try {
            NotificationDto existing = objectMapper.readValue(json, NotificationDto.class);
            existing.setStatus(0);
            existing.setLastUpdated(LocalDateTime.now());
            String newJson = objectMapper.writeValueAsString(existing);
            redisTemplate.opsForValue().set(key, newJson);
            // remove from account set & global? keep in global but mark status=0; keep consistent
            if (existing.getAccountId() != null) {
                redisTemplate.opsForZSet().remove(accountSetKey(existing.getAccountId()), id.toString());
            }
            redisTemplate.opsForZSet().remove(GLOBAL_SET, id.toString());
            redisTemplate.delete(key); // optional: remove actual key
            return "Xóa thành công Notification với ID: " + id;
        } catch (Exception e) {
            throw new ResourceNotFoundException("Notification not found");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "notifications", allEntries = true)
    public NotificationDto changeStatus(UUID id, Integer status) {
        String key = notificationKey(id);
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) throw new ResourceNotFoundException("Notification not found");
        try {
            NotificationDto existing = objectMapper.readValue(json, NotificationDto.class);
            existing.setStatus(status);
            existing.setLastUpdated(LocalDateTime.now());
            String newJson = objectMapper.writeValueAsString(existing);
            redisTemplate.opsForValue().set(key, newJson);
            return existing;
        } catch (Exception e) {
            log.error("Failed to change status in Redis", e);
            throw new RuntimeException("Failed to change status");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "notifications", allEntries = true)
    public NotificationDto readNotification(UUID id) {
        String key = notificationKey(id);
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) throw new ResourceNotFoundException("Notification not found");
        try {
            NotificationDto existing = objectMapper.readValue(json, NotificationDto.class);
            existing.setIsRead(true);
            existing.setLastUpdated(LocalDateTime.now());
            String newJson = objectMapper.writeValueAsString(existing);
            redisTemplate.opsForValue().set(key, newJson);
            return existing;
        } catch (Exception e) {
            log.error("Failed to mark notification read in Redis", e);
            throw new RuntimeException("Failed to read notification");
        }
    }
}
