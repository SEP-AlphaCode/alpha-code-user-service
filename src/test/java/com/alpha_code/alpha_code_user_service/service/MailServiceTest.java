package com.alpha_code.alpha_code_user_service.service;

import com.alpha_code.alpha_code_user_service.service.impl.MailServiceImpl;
import com.alpha_code.alpha_code_user_service.util.EmailBody;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailBody emailBody;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private MailServiceImpl mailService;

    @Test
    void testSendPaymentSuccessEmail_Success() throws MessagingException {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        mailService.sendPaymentSuccessEmail(
            "test@example.com",
            "Test User",
            "Test Service",
            12345L,
            100000
        );

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendCourseCompletedEmail_Success() throws MessagingException {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(emailBody.getCourseCompletedEmailBody(anyString(), anyString(), anyString(), anyString()))
            .thenReturn("<html>Course completed</html>");

        mailService.sendCourseCompletedEmail(
            "test@example.com",
            "Test User",
            "Test Course",
            "course123",
            "account123"
        );

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }
}

