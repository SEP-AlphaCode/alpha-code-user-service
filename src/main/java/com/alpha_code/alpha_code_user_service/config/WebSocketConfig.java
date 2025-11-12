package com.alpha_code.alpha_code_user_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        logger.info("🔌 Registering STOMP WebSocket endpoint: /ws");

        // ✅ Native WebSocket endpoint
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");

        // ✅ SockJS fallback endpoint
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        logger.info("✅ STOMP WebSocket endpoints registered (native + SockJS fallback)");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        logger.info("📡 Configuring message broker");

        // Client gửi lên server (ví dụ: /app/sendMessage)
        registry.setApplicationDestinationPrefixes("/app");

        // Server gửi về client (ví dụ: /topic/notifications)
        registry.enableSimpleBroker("/topic", "/queue");

        logger.info("✅ Message broker configured successfully");
    }
}
