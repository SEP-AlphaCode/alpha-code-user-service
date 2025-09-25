package com.alpha_code.alpha_code_user_service.service;

import com.alpha_code.alpha_code_user_service.dto.NotificationDto;
import com.alpha_code.alpha_code_user_service.dto.PagedResult;
import com.alpha_code.alpha_code_user_service.entity.Notification;

import java.util.UUID;

public interface NotificationService {
    PagedResult<NotificationDto> getAll(int page, int size, UUID accountId, Integer status);

    NotificationDto getById(UUID id);

    NotificationDto create(NotificationDto notificationDto);

    NotificationDto update(UUID id, NotificationDto notificationDto);

    NotificationDto patchUpdate(UUID id, NotificationDto notificationDto);

    String delete(UUID id);

    NotificationDto changeStatus(UUID id, Integer status);

    NotificationDto readNotification(UUID id);
}
