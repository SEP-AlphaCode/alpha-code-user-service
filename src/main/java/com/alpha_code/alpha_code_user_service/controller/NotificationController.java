package com.alpha_code.alpha_code_user_service.controller;

import com.alpha_code.alpha_code_user_service.dto.NotificationDto;
import com.alpha_code.alpha_code_user_service.dto.PagedResult;
import com.alpha_code.alpha_code_user_service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications")
@Validated
public class NotificationController {

    private final NotificationService service;

    @GetMapping
    @Operation(summary = "Get all notifications with pagination and optional filters")
    public PagedResult<NotificationDto> getAll(@RequestParam(value = "page", defaultValue = "1") int page,
                                               @RequestParam(value = "size", defaultValue = "10") int size,
                                               @RequestParam(value = "accountId", required = false) UUID accountId,
                                               @RequestParam(value = "status", required = false) Integer status) {
        return service.getAll(page, size, accountId, status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification by id")
    public NotificationDto getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping
    @Operation(summary = "Create new notification")
    public NotificationDto create(@RequestBody NotificationDto notificationDto) {
        return service.create(notificationDto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update notification by id")
    public NotificationDto update(@PathVariable UUID id, @RequestBody NotificationDto notificationDto) {
        return service.update(id, notificationDto);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Patch update notification by id")
    public NotificationDto patchUpdate(@PathVariable UUID id, @RequestBody NotificationDto notificationDto) {
        return service.patchUpdate(id, notificationDto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete notification by id")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @PatchMapping("/{id}/change-status")
    @Operation(summary = "Change notification status")
    public NotificationDto changeStatus(@PathVariable UUID id, @RequestParam Integer status) {
        return service.changeStatus(id, status);
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Read notification")
    public NotificationDto readNotification(@PathVariable UUID id) {
        return service.readNotification(id);
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read for a specific account")
    public java.util.Map<String, Object> readAllNotifications(@RequestParam UUID accountId) {
        return service.readAllNotifications(accountId);
    }
}
