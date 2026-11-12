package com.portfolio.security;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.portfolio.entity.User;
import com.portfolio.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2AuthenticationSuccessHandler")
class OAuth2AuthenticationSuccessHandlerTest {

    private static final String SECRET_KEY = "test-secret-key-minimum-32-characters-long-for-testing";
    private static final long EXPIRATION_MS = 3_600_000L;
    private static final String NOT_AUTHORIZED = "http://localhost:3000/admin/login?error=not_authorized";

    @Mock
    private UserService userService;

    private JwtService jwtService;
    private OAuth2AuthenticationSuccessHandler handler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private final Logger handlerLogger = (Logger) LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);
    private final ListAppender<ILoggingEvent> logEvents = new ListAppender<>();

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(new MockEnvironment());
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION_MS);
        jwtService.init();
        handler = new OAuth2AuthenticationSuccessHandler(jwtService, userService);
        ReflectionTestUtils.setField(handler, "frontendUrl", "http://localhost:3000");
        request = new MockHttpServletRequest("GET", "/login/oauth2/code/google");
        response = new MockHttpServletResponse();
        logEvents.start();
        handlerLogger.addAppender(logEvents);
    }

    @AfterEach
    void detachAppender() {
        handlerLogger.detachAppender(logEvents);
    }

    private OAuth2AuthenticationToken googleLogin(String email, Object emailVerified) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "google-sub-1");
        attributes.put("email", email);
        attributes.put("name", "Some Name");
        attributes.put("picture", "https://example.com/avatar.png");
        if (emailVerified != null) {
            attributes.put("email_verified", emailVerified);
        }
        DefaultOAuth2User user = new DefaultOAuth2User(List.of(new OAuth2UserAuthority(attributes)), attributes, "email");
        return new OAuth2AuthenticationToken(user, user.getAuthorities(), "google");
    }

    private void givenProcessedUser(String email, boolean isAdmin) {
        given(userService.processOAuthUser(any(), any(), any(), any(), any()))
                .willReturn(isAdmin
                        ? Optional.of(User.builder().email(email).name("Some Name").isAdmin(true).build())
                        : Optional.empty());
    }

    private List<ILoggingEvent> events(Level level) {
        return logEvents.list.stream().filter(event -> event.getLevel() == level).toList();
    }

    @Nested
    @DisplayName("unverified Google e-mail")
    class UnverifiedEmail {

        @Test
        @DisplayName("email_verified=false is rejected before the user is processed")
        void falseIsRejected() throws Exception {
            handler.onAuthenticationSuccess(request, response, googleLogin("admin@test.com", false));

            assertThat(response.getRedirectedUrl()).isEqualTo(NOT_AUTHORIZED);
            assertThat(response.getHeaders(HttpHeaders.SET_COOKIE)).isEmpty();
            verify(userService, never()).processOAuthUser(any(), any(), any(), any(), any());
            assertThat(events(Level.WARN)).singleElement()
                    .satisfies(event -> assertThat(event.getFormattedMessage()).contains("admin@test.com"));
        }

        @Test
        @DisplayName("missing email_verified is rejected before the user is processed")
        void missingIsRejected() throws Exception {
            handler.onAuthenticationSuccess(request, response, googleLogin("admin@test.com", null));

            assertThat(response.getRedirectedUrl()).isEqualTo(NOT_AUTHORIZED);
            assertThat(response.getHeaders(HttpHeaders.SET_COOKIE)).isEmpty();
            verify(userService, never()).processOAuthUser(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("a non-boolean email_verified value is rejected")
        void stringValueIsRejected() throws Exception {
            handler.onAuthenticationSuccess(request, response, googleLogin("admin@test.com", "yes"));

            assertThat(response.getRedirectedUrl()).isEqualTo(NOT_AUTHORIZED);
            verify(userService, never()).processOAuthUser(any(), any(), any(), any(), any());
        }
    }

    @Test
    @DisplayName("a GitHub-style login without an e-mail is rejected: no '<login>@github.user' fallback")
    void githubStyleLoginIsRejected() throws Exception {
        Map<String, Object> attributes = Map.of("id", 42, "login", "admin", "name", "Admin");
        DefaultOAuth2User user = new DefaultOAuth2User(List.of(new OAuth2UserAuthority(attributes)), attributes, "login");

        handler.onAuthenticationSuccess(request, response,
                new OAuth2AuthenticationToken(user, user.getAuthorities(), "github"));

        assertThat(response.getRedirectedUrl()).isEqualTo(NOT_AUTHORIZED);
        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE)).isEmpty();
        verify(userService, never()).processOAuthUser(any(), any(), any(), any(), any());
    }

    @Nested
    @DisplayName("verified Google e-mail")
    class VerifiedEmail {

        @Test
        @DisplayName("admin gets an HttpOnly SameSite=Lax cookie, a redirect to /admin and an INFO audit line")
        void adminGetsCookie() throws Exception {
            givenProcessedUser("admin@test.com", true);

            handler.onAuthenticationSuccess(request, response, googleLogin("admin@test.com", true));

            assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost:3000/admin");
            String cookie = response.getHeader(HttpHeaders.SET_COOKIE);
            assertThat(cookie).startsWith("auth_token=")
                    .contains("HttpOnly", "SameSite=Lax", "Path=/", "Max-Age=3600")
                    .doesNotContain("Secure");
            String token = cookie.substring("auth_token=".length(), cookie.indexOf(';'));
            assertThat(jwtService.extractEmail(token)).isEqualTo("admin@test.com");
            assertThat(events(Level.INFO)).singleElement()
                    .satisfies(event -> assertThat(event.getFormattedMessage())
                            .contains("admin@test.com")
                            .doesNotContain(token));
            verify(userService).processOAuthUser("admin@test.com", "Some Name",
                    "https://example.com/avatar.png", "google", "google-sub-1");
        }

        @Test
        @DisplayName("cookie is Secure when the frontend runs on https")
        void secureCookieForHttpsFrontend() throws Exception {
            ReflectionTestUtils.setField(handler, "frontendUrl", "https://jbytow.pl");
            givenProcessedUser("admin@test.com", true);

            handler.onAuthenticationSuccess(request, response, googleLogin("admin@test.com", true));

            assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).contains("Secure");
            assertThat(response.getRedirectedUrl()).isEqualTo("https://jbytow.pl/admin");
        }

        @Test
        @DisplayName("non-admin is redirected with not_authorized, gets no cookie and leaves a WARN line")
        void nonAdminIsRejected() throws Exception {
            givenProcessedUser("someone@test.com", false);

            handler.onAuthenticationSuccess(request, response, googleLogin("someone@test.com", true));

            assertThat(response.getRedirectedUrl()).isEqualTo(NOT_AUTHORIZED);
            assertThat(response.getHeaders(HttpHeaders.SET_COOKIE)).isEmpty();
            assertThat(events(Level.WARN)).singleElement()
                    .satisfies(event -> assertThat(event.getFormattedMessage()).contains("someone@test.com"));
        }
    }
}
