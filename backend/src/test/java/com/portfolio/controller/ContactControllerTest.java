package com.portfolio.controller;

import com.portfolio.config.TestSecurityConfig;
import com.portfolio.security.JwtService;
import com.portfolio.service.ContactService;
import com.portfolio.service.UserService;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ContactController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class
        }
)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("ContactController")
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactService contactService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserService userService;

    private static String body(String name, String email, String subject, String message) {
        return """
                {"name": "%s", "email": "%s", "subject": "%s", "message": "%s"}
                """.formatted(name, email, subject, message);
    }

    private static String bodyWithWebsite(String website) {
        return """
                {"name": "Anna", "email": "anna@example.org", "subject": "Hello", "message": "Nice portfolio", "website": "%s"}
                """.formatted(website);
    }

    @Nested
    @DisplayName("honeypot field")
    class Honeypot {

        private final Logger controllerLogger = (Logger) LoggerFactory.getLogger(ContactController.class);
        private final ListAppender<ILoggingEvent> logEvents = new ListAppender<>();

        @BeforeEach
        void attachAppender() {
            logEvents.start();
            controllerLogger.addAppender(logEvents);
        }

        @AfterEach
        void detachAppender() {
            controllerLogger.detachAppender(logEvents);
        }

        @ParameterizedTest(name = "website = {0}")
        @ValueSource(strings = {"http://spam", "x", "  spam  "})
        @DisplayName("a filled website field gets the normal 200 envelope but nothing is sent")
        void filledHoneypotIsSilentlyDropped(String website) throws Exception {
            mockMvc.perform(post("/api/contact")
                            .header("CF-Connecting-IP", "203.0.113.7")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bodyWithWebsite(website)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").doesNotExist())
                    .andExpect(jsonPath("$.errors").doesNotExist());

            verify(contactService, never()).sendContactEmail(any());
            assertThat(logEvents.list)
                    .filteredOn(event -> event.getLevel() == Level.WARN)
                    .singleElement()
                    .satisfies(event -> assertThat(event.getFormattedMessage())
                            .contains("Contact honeypot triggered")
                            .contains("203.0.113.7"));
        }

        @ParameterizedTest(name = "website = \"{0}\"")
        @ValueSource(strings = {"", "   "})
        @DisplayName("an empty or blank website field is a normal submission")
        void blankHoneypotIsSent(String website) throws Exception {
            mockMvc.perform(post("/api/contact")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bodyWithWebsite(website)))
                    .andExpect(status().isOk());

            verify(contactService, times(1)).sendContactEmail(any());
            assertThat(logEvents.list).isEmpty();
        }

        @Test
        @DisplayName("a website value over 200 characters is rejected with 400 and sends nothing")
        void oversizedHoneypotIsRejected() throws Exception {
            mockMvc.perform(post("/api/contact")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bodyWithWebsite("x".repeat(201))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.website").exists());

            verify(contactService, never()).sendContactEmail(any());
        }
    }

    @Test
    @DisplayName("a valid submission returns 200 and hands the request to ContactService once")
    void validSubmissionIsSent() throws Exception {
        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Anna", "anna@example.org", "Hello", "Nice portfolio")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(contactService, times(1)).sendContactEmail(any());
    }

    @Test
    @DisplayName("an invalid e-mail returns 400 with a field error and sends nothing")
    void invalidEmailIsRejected() throws Exception {
        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Anna", "not-an-email", "Hello", "Nice portfolio")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.email").exists());

        verify(contactService, never()).sendContactEmail(any());
    }

    @Test
    @DisplayName("a blank message returns 400 with a field error and sends nothing")
    void blankMessageIsRejected() throws Exception {
        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Anna", "anna@example.org", "Hello", "   ")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.message").exists());

        verify(contactService, never()).sendContactEmail(any());
    }

    @Test
    @DisplayName("an oversized message returns 400 and sends nothing")
    void oversizedMessageIsRejected() throws Exception {
        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Anna", "anna@example.org", "Hello", "x".repeat(5001))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.message").exists());

        verify(contactService, never()).sendContactEmail(any());
    }

    @Test
    @DisplayName("a non-JSON body returns 400 instead of 500")
    void malformedBodyIsRejected() throws Exception {
        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not json"))
                .andExpect(status().isBadRequest());

        verify(contactService, never()).sendContactEmail(any());
    }
}
