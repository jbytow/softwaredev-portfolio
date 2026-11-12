package com.portfolio.service;

import com.portfolio.dto.ContactRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService {

    private final JavaMailSender mailSender;

    @Value("${app.contact.recipient}")
    private String recipientEmail;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Compose passes empty strings for unset CONTACT_EMAIL / MAIL_USERNAME, so the app starts fine
     * and every submission would fail; warn once at startup instead.
     */
    @PostConstruct
    void warnAboutMissingMailConfig() {
        if (recipientEmail == null || recipientEmail.isBlank() || fromEmail == null || fromEmail.isBlank()) {
            log.warn("Contact form mail is not configured: app.contact.recipient or spring.mail.username is blank, "
                    + "submissions will fail");
        }
    }

    public void sendContactEmail(ContactRequest request) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipientEmail);
        message.setFrom(fromEmail);
        message.setReplyTo(request.getEmail());
        message.setSubject("[Portfolio Contact] " + request.getSubject());
        message.setText(buildEmailBody(request));

        try {
            mailSender.send(message);
            log.info("Contact email sent");
        } catch (MailException e) {
            // Logged once, with the cause, by GlobalExceptionHandler
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Message could not be sent", e);
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
