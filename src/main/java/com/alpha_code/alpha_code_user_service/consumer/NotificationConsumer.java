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
        UUID accountId = (UUID) message.get("accountId");
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



}
