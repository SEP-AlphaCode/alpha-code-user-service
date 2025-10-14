package com.alpha_code.alpha_code_user_service.service;

import jakarta.mail.MessagingException;

public interface MailService {
    void sendPaymentSuccessEmail(
            String to,
            String fullName,
            String serviceName,
            Long orderCode,
            Integer price
    ) throws MessagingException;
}
