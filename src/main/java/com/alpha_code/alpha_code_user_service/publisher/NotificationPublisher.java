package com.alpha_code.alpha_code_user_service.publisher;

import com.alpha_code.alpha_code_user_service.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendToUser(UUID accountId, NotificationDto notification) {
        // Gửi tới topic riêng của user (client phải subscribe đúng đường này)
        String destination = "/topic/notifications/" + accountId;
        messagingTemplate.convertAndSend(destination, notification);
    }

    public void sendToUsers(List<UUID> accountIds, NotificationDto notification) {
        for (UUID accountId : accountIds) {
            sendToUser(accountId, notification);
        }
    }

}
