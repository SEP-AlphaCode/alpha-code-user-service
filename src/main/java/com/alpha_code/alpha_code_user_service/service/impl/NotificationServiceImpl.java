package com.alpha_code.alpha_code_user_service.service.impl;

import com.alpha_code.alpha_code_user_service.dto.NotificationDto;
import com.alpha_code.alpha_code_user_service.dto.PagedResult;
import com.alpha_code.alpha_code_user_service.entity.Notification;
import com.alpha_code.alpha_code_user_service.enums.NotificationTypeEnum;
import com.alpha_code.alpha_code_user_service.exception.ResourceNotFoundException;
import com.alpha_code.alpha_code_user_service.mapper.NotificationMapper;
import com.alpha_code.alpha_code_user_service.publisher.NotificationPublisher;
import com.alpha_code.alpha_code_user_service.repository.AccountRepository;
import com.alpha_code.alpha_code_user_service.repository.NotificationRepository;
import com.alpha_code.alpha_code_user_service.service.MailService;
import com.alpha_code.alpha_code_user_service.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;
    private final NotificationPublisher notificationPublisher;
    private final AccountRepository accountRepository;
    private final MailService mailService;

    @Override
    @Cacheable(value = "notifications_list", key = "{#page, #size, #accountId, #status}")
    public PagedResult<NotificationDto> getAll(int page, int size, UUID accountId, Integer status) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Notification> pageResult;

        if (accountId != null && status != null) {
            pageResult = repository.findAllByAccountIdAndStatus(accountId, status, pageable);
        } else if (accountId != null) {
            pageResult = repository.findAllByAccountId(accountId, pageable);
        } else if (status != null) {
            pageResult = repository.findAllByStatus(status, pageable);
        } else {
            pageResult = repository.findAll(pageable);
        }
        return new PagedResult<>(pageResult.map(NotificationMapper::toDto));
    }

    @Override
    @Cacheable(value = "notifications", key = "#id")
    public NotificationDto getById(UUID id) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        return NotificationMapper.toDto(entity);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"notifications_list", "notifications"}, allEntries = true)
    public NotificationDto create(NotificationDto notificationDto) {
        var entity = NotificationMapper.toEntity(notificationDto);
        entity.setCreatedDate(LocalDateTime.now());

        Notification savedEntity = repository.save(entity);
        NotificationDto result = NotificationMapper.toDto(savedEntity);

        // Bắn socket realtime tới client
        notificationPublisher.sendToUser(result.getAccountId(), notificationDto);

        try {
            // Lấy enum từ type code
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

        } catch (Exception e) {
            log.error("Lỗi khi gửi email thông báo thanh toán: ", e);
        }

        return result;
    }

    @Override
    @Transactional
    @CacheEvict(value = "notifications_list", allEntries = true)
    @CachePut(value = "notifications", key = "#notificationDto.id")
    public NotificationDto update(UUID id, NotificationDto notificationDto) {
        Notification entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        entity.setAccountId(notificationDto.getAccountId());
        entity.setType(notificationDto.getType());
        entity.setTitle(notificationDto.getTitle());
        entity.setMessage(notificationDto.getMessage());
        entity.setIsRead(notificationDto.getIsRead());
        entity.setStatus(notificationDto.getStatus());
        entity.setLastUpdated(LocalDateTime.now());
        Notification savedEntity = repository.save(entity);
        return NotificationMapper.toDto(savedEntity);
    }

    @Override
    @Transactional
    @CacheEvict(value = "notifications_list", allEntries = true)
    @CachePut(value = "notifications", key = "#notificationDto.id")
    public NotificationDto patchUpdate(UUID id, NotificationDto notificationDto) {
        Notification entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (notificationDto.getAccountId() != null) {
            entity.setAccountId(notificationDto.getAccountId());
        }
        if (notificationDto.getType() != null) {
            entity.setType(notificationDto.getType());
        }
        if (notificationDto.getTitle() != null) {
            entity.setTitle(notificationDto.getTitle());
        }
        if (notificationDto.getMessage() != null) {
            entity.setMessage(notificationDto.getMessage());
        }
        if (notificationDto.getIsRead() != null) {
            entity.setIsRead(notificationDto.getIsRead());
        }
        if (notificationDto.getStatus() != null) {
            entity.setStatus(notificationDto.getStatus());
        }
        entity.setLastUpdated(LocalDateTime.now());
        Notification savedEntity = repository.save(entity);
        return NotificationMapper.toDto(savedEntity);
    }

    @Override
    @Transactional
    @CacheEvict(value = "{notifications_list, notifications}", allEntries = true)
    public String delete(UUID id) {
        try {
            Notification entity = repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));


            entity.setStatus(0);
            entity.setLastUpdated(LocalDateTime.now());
            repository.save(entity);
            return "Xóa thành công Notification với ID: " + id;
        } catch (Exception e) {
            throw new ResourceNotFoundException("Notification not found");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "notifications", allEntries = true)
    public NotificationDto changeStatus(UUID id, Integer status) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        entity.setStatus(status);
        entity.setLastUpdated(LocalDateTime.now());
        Notification savedEntity = repository.save(entity);
        return NotificationMapper.toDto(savedEntity);
    }

    @Override
    @Transactional
    @CacheEvict(value = "notifications", allEntries = true)
    public NotificationDto readNotification(UUID id) {
        var notification = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notification.setIsRead(true);
        notification.setLastUpdated(LocalDateTime.now());
        Notification savedEntity = repository.save(notification);
        return NotificationMapper.toDto(savedEntity);
    }
}
