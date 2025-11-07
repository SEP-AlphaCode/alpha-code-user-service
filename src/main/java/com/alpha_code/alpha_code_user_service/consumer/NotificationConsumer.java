package com.alpha_code.alpha_code_user_service.consumer;

import com.alpha_code.alpha_code_user_service.dto.NotificationDto;
import com.alpha_code.alpha_code_user_service.enums.NotificationTypeEnum;
import com.alpha_code.alpha_code_user_service.service.NotificationService;
import com.alpha_code.alpha_code_user_service.util.PaymentNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {
    private final NotificationService notificationService;

    @RabbitListener(
            queues = "notification.send.queue",
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void handleCreateNotification(Map<String, Object> message) {
        log.info("Received message from notification.create.queue: {}", message);
        Long orderCode = ((Number) message.get("orderCode")).longValue();
        String accountIdStr = (String) message.get("accountId");
        UUID accountId = UUID.fromString(accountIdStr);
        String serviceName = (String) message.get("serviceName");
        Integer price = (Integer) message.get("price");

        var noti = new NotificationDto();
        noti.setAccountId(accountId);
        noti.setTitle(PaymentNotification.getTitle(serviceName));
        noti.setMessage(PaymentNotification.getMessage(orderCode, serviceName, price));
        noti.setType(NotificationTypeEnum.PAYMENT_SUCCESS.getCode());
        noti.setPrice(price);
        noti.setOrderCode(orderCode);
        noti.setServiceName(serviceName);
        noti.setStatus(1);

        notificationService.create(noti);
    }

    // --- New listener for course completion ---
    @RabbitListener(
            queues = "course.completed.queue",
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void handleCourseCompleted(Map<String, Object> message) {
        log.info("Received message from course.completed.queue: {}", message);

        String accountIdStr = (String) message.get("accountId").toString();
        UUID accountId = UUID.fromString(accountIdStr);
        String courseIdStr = (String) message.get("courseId");
        String courseName = (String) message.get("courseName");



        var noti = new NotificationDto();
        noti.setAccountId(accountId);
        noti.setTitle("Hoàn thành khóa học");
        noti.setMessage(courseIdStr);
        noti.setType(NotificationTypeEnum.FINISHCOURSE.getCode());
        noti.setServiceName(courseName);
        noti.setStatus(1);

        notificationService.create(noti);

        log.info("Notification for course completion created for accountId={}, courseId={}", accountId, courseIdStr);
    }



}
