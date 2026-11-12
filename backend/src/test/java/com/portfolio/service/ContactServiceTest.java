package com.portfolio.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.portfolio.dto.ContactRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContactService")
class ContactServiceTest {

    private static final String VISITOR_EMAIL = "visitor@example.org";

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private ContactService contactService;

    private final Logger serviceLogger = (Logger) LoggerFactory.getLogger(ContactService.class);
    private final ListAppender<ILoggingEvent> logEvents = new ListAppender<>();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(contactService, "recipientEmail", "owner@example.com");
        ReflectionTestUtils.setField(contactService, "fromEmail", "sender@example.com");
        logEvents.start();
        serviceLogger.addAppender(logEvents);
    }

    @AfterEach
    void detachLogAppender() {
        serviceLogger.detachAppender(logEvents);
    }

    private ContactRequest request() {
        ContactRequest request = new ContactRequest();
        request.setName("Anna Visitor");
        request.setEmail(VISITOR_EMAIL);
        request.setSubject("Collaboration");
        request.setMessage("Hello there");
        return request;
    }

    @Test
    @DisplayName("sends one message to the owner with reply-to set to the visitor")
    void sendsMessageToRecipientWithVisitorReplyTo() {
        contactService.sendContactEmail(request());

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getTo()).containsExactly("owner@example.com");
        assertThat(sent.getFrom()).isEqualTo("sender@example.com");
        assertThat(sent.getReplyTo()).isEqualTo(VISITOR_EMAIL);
        assertThat(sent.getSubject()).isEqualTo("[Portfolio Contact] Collaboration");
        assertThat(sent.getText()).contains("Anna Visitor").contains(VISITOR_EMAIL).contains("Hello there");
    }

    @Test
    @DisplayName("does not write the visitor address to the log on success")
    void successLogDoesNotContainVisitorAddress() {
        contactService.sendContactEmail(request());

        assertThat(logEvents.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .noneMatch(message -> message.contains(VISITOR_EMAIL));
    }

    @Test
    @DisplayName("mail failure becomes a 503 carrying the cause and is not logged by the service")
    void mailFailureBecomesServiceUnavailable() {
        MailSendException cause = new MailSendException("smtp rejected " + VISITOR_EMAIL);
        doThrow(cause).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> contactService.sendContactEmail(request()))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                    assertThat(ex.getReason()).isEqualTo("Message could not be sent");
                    assertThat(ex.getCause()).isSameAs(cause);
                });

        assertThat(logEvents.list).isEmpty();
    }

    @ParameterizedTest(name = "recipient=[{0}] sender=[{1}]")
    @CsvSource(value = {"'', sender@example.com", "owner@example.com, ''", "'', ''"})
    @DisplayName("blank recipient or sender logs one WARN at startup")
    void blankMailConfigLogsOneWarn(String recipient, String sender) {
        ReflectionTestUtils.setField(contactService, "recipientEmail", recipient);
        ReflectionTestUtils.setField(contactService, "fromEmail", sender);

        contactService.warnAboutMissingMailConfig();

        assertThat(logEvents.list).hasSize(1);
        assertThat(logEvents.list.get(0).getLevel()).isEqualTo(Level.WARN);
    }

    @Test
    @DisplayName("complete mail config logs nothing at startup")
    void completeMailConfigLogsNothing() {
        contactService.warnAboutMissingMailConfig();

        assertThat(logEvents.list).isEmpty();
    }
}
