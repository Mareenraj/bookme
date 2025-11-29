package com.mareen.bookme.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendOtpEmail(String to, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Email Verification - BookMe");
            message.setText(buildOtpEmailContent(otp));

            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", to, e);
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    private String buildOtpEmailContent(String otp) {
        return """
                Hello,
                
                Your verification code for BookMe is: %s
                
                This code will expire in 5 minutes.
                
                If you didn't request this code, please ignore this email.
                
                Best regards,
                BookMe Team
                """.formatted(otp);
    }

    @Async
    public void sendWelcomeEmail(String to, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Welcome to BookMe!");
            message.setText(buildWelcomeEmailContent(username));

            mailSender.send(message);
            log.info("Welcome email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", to, e);
        }
    }

    private String buildWelcomeEmailContent(String username) {
        return """
                Hello %s,
                
                Welcome to BookMe! Your email has been successfully verified.
                
                You can now log in and start using our services.
                
                Best regards,
                BookMe Team
                """.formatted(username);
    }
}
