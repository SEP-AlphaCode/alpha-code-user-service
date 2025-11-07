package com.alpha_code.alpha_code_user_service.service.impl;

import com.alpha_code.alpha_code_user_service.service.MailService;
import com.alpha_code.alpha_code_user_service.util.EmailBody;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements MailService {
    private final JavaMailSender mailSender;

    @Override
    public void sendPaymentSuccessEmail(String to, String fullName, String serviceName, Long orderCode, Integer price)
            throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("Xác nhận thanh toán thành công - AlphaCode");

        //  Tạo nội dung HTML
        String emailContent = EmailBody.getPaymentSuccessEmailBody(fullName, serviceName, orderCode, price);
        helper.setText(emailContent, true);

        //  Gắn logo (inline image)
        ClassPathResource logoImage = new ClassPathResource("images/alphacode-logo.png");
        if (logoImage.exists()) {
            helper.addInline("alphacode-logo", logoImage);
        }

        //  Gửi email
        mailSender.send(message);
        log.info("Đã gửi email thanh toán thành công tới {}", to);
    }

    @Override
    public void sendCourseCompletedEmail(String to, String fullName, String courseName, String courseId)
            throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("Chúc mừng bạn đã hoàn thành khóa học - AlphaCode");

        // Nội dung email HTML
        String emailContent = EmailBody.getCourseCompletedEmailBody(fullName, courseName, courseId);
        helper.setText(emailContent, true);

        // Gắn logo inline
        ClassPathResource logoImage = new ClassPathResource("images/alphacode-logo.png");
        if (logoImage.exists()) {
            helper.addInline("alphacode-logo", logoImage);
        }

        // Gửi email
        mailSender.send(message);
        log.info("Đã gửi email hoàn thành khóa học tới {}", to);
    }

}
