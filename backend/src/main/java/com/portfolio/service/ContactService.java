package com.portfolio.service;

import com.portfolio.dto.ContactRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService {

    private final JavaMailSender mailSender;

    @Value("${app.contact.recipient}")
    private String recipientEmail;

    @Value("${spring.mail.username:noreply@portfolio.com}")
    private String fromEmail;

    public void sendContactEmail(ContactRequest request) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipientEmail);
        message.setFrom(fromEmail);
        message.setReplyTo(request.getEmail());
        message.setSubject("[Portfolio Contact] " + request.getSubject());
        message.setText(buildEmailBody(request));

        try {
            mailSender.send(message);
            log.info("Contact email sent successfully from: {}", request.getEmail());
        } catch (MailException e) {
            log.error("Failed to send contact email from: {}", request.getEmail(), e);
            throw new RuntimeException("Failed to send email. Please try again later.");
        }
    }

    private String buildEmailBody(ContactRequest request) {
        return String.format("""
                You have received a new contact form submission:

                From: %s
                Email: %s
                Subject: %s

                Message:
                %s
                """,
                request.getName(),
                request.getEmail(),
                request.getSubject(),
                request.getMessage()
        );
    }
}
