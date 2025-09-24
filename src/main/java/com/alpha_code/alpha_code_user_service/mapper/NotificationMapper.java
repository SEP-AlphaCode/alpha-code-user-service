package com.alpha_code.alpha_code_user_service.mapper;

import com.alpha_code.alpha_code_user_service.dto.NotificationDto;
import com.alpha_code.alpha_code_user_service.entity.Notification;

public class NotificationMapper {
    public static NotificationDto toDto(Notification notification) {
        if (notification == null) {
            return null;
        }

        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setCreatedDate(notification.getCreatedDate());
        dto.setAccountId(notification.getAccountId());
        if (notification.getAccount() != null) {
            dto.setAccountFullName(notification.getAccount().getFullName());
        }
        dto.setType(notification.getType());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setIsRead(notification.getIsRead());
        dto.setStatus(notification.getStatus());
        dto.setLastUpdated(notification.getLastUpdated());
        return dto;
    }

    public static Notification toEntity(NotificationDto dto) {
        if (dto == null) {
            return null;
        }

        Notification notification = new Notification();
        notification.setId(dto.getId());
        notification.setCreatedDate(dto.getCreatedDate());
        notification.setAccountId(dto.getAccountId());
        notification.setType(dto.getType());
        notification.setTitle(dto.getTitle());
        notification.setMessage(dto.getMessage());
        notification.setIsRead(dto.getIsRead());
        notification.setStatus(dto.getStatus());
        notification.setLastUpdated(dto.getLastUpdated());
        return notification;
    }
}
