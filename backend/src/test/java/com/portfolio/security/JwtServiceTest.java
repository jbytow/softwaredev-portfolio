package com.portfolio.security;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtService")
class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET_KEY = "test-secret-key-minimum-32-characters-long-for-testing";
    private static final long EXPIRATION = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtService = newService(SECRET_KEY, new MockEnvironment());
        jwtService.init();
    }

    private static JwtService newService(String secret, MockEnvironment environment) {
        JwtService service = new JwtService(environment);
        ReflectionTestUtils.setField(service, "secretKey", secret);
        ReflectionTestUtils.setField(service, "jwtExpiration", EXPIRATION);
        return service;
    }

    private static MockEnvironment prodEnvironment() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        return environment;
    }

    @Nested
    @DisplayName("init")
    class Init {

        @Test
        @DisplayName("rejects a secret shorter than 32 bytes at startup")
        void rejectsShortSecret() {
            JwtService service = newService("a".repeat(31), new MockEnvironment());

            assertThatThrownBy(service::init).isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("rejects an empty secret at startup")
        void rejectsEmptySecret() {
            JwtService service = newService("", new MockEnvironment());

            assertThatThrownBy(service::init).isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("counts UTF-8 bytes, not characters: 16 two-byte characters are enough")
        void countsUtf8Bytes() {
            JwtService service = newService("ż".repeat(16), new MockEnvironment());

            assertThatCode(service::init).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("rejects the committed development secret in the prod profile")
        void rejectsDevDefaultInProd() {
            JwtService service = newService(JwtService.DEV_DEFAULT_SECRET, prodEnvironment());

            assertThatThrownBy(service::init)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageNotContaining(JwtService.DEV_DEFAULT_SECRET);
        }

        @Test
        @DisplayName("accepts the development secret outside the prod profile")
        void acceptsDevDefaultOutsideProd() {
            JwtService service = newService(JwtService.DEV_DEFAULT_SECRET, new MockEnvironment());

            assertThatCode(service::init).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("accepts a long custom secret in the prod profile")
        void acceptsCustomSecretInProd() {
            JwtService service = newService(SECRET_KEY, prodEnvironment());

            assertThatCode(service::init).doesNotThrowAnyException();
        }
    }

    @Test
    @DisplayName("getExpiration returns the configured lifetime")
    void expirationMatchesConfiguration() {
        assertThat(jwtService.getExpiration()).isEqualTo(Duration.ofMillis(EXPIRATION));
    }

    @Nested
    @DisplayName("generateToken")
    class GenerateToken {

        @Test
        @DisplayName("should generate a valid JWT token")
        void shouldGenerateValidToken() {
            String email = "test@example.com";
            String name = "Test User";
            boolean isAdmin = false;

            String token = jwtService.generateToken(email, name, isAdmin);

            assertThat(token).isNotNull().isNotEmpty();
            assertThat(jwtService.isTokenValid(token)).isTrue();
        }

        @Test
        @DisplayName("should include email as subject in token")
        void shouldIncludeEmailAsSubject() {
            String email = "test@example.com";

            String token = jwtService.generateToken(email, "Test User", false);

            assertThat(jwtService.extractEmail(token)).isEqualTo(email);
        }

        @Test
        @DisplayName("should include name in token claims")
        void shouldIncludeNameInClaims() {
            String name = "Test User";

            String token = jwtService.generateToken("test@example.com", name, false);

            assertThat(jwtService.extractName(token)).isEqualTo(name);
        }

        @Test
        @DisplayName("should include isAdmin flag in token claims")
        void shouldIncludeIsAdminInClaims() {
            String token = jwtService.generateToken("admin@example.com", "Admin", true);

            assertThat(jwtService.extractIsAdmin(token)).isTrue();
        }

        @Test
        @DisplayName("should set isAdmin to false when user is not admin")
        void shouldSetIsAdminFalseForNonAdmin() {
            String token = jwtService.generateToken("user@example.com", "User", false);

            assertThat(jwtService.extractIsAdmin(token)).isFalse();
        }
    }

    @Nested
    @DisplayName("extractEmail")
    class ExtractEmail {

        @Test
        @DisplayName("should extract email from valid token")
        void shouldExtractEmailFromValidToken() {
            String email = "extract.test@example.com";
            String token = jwtService.generateToken(email, "Test", false);

            String extractedEmail = jwtService.extractEmail(token);

            assertThat(extractedEmail).isEqualTo(email);
        }

        @Test
        @DisplayName("should throw exception for invalid token")
        void shouldThrowExceptionForInvalidToken() {
            String invalidToken = "invalid.token.here";

            assertThatThrownBy(() -> jwtService.extractEmail(invalidToken))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("isTokenValid")
    class IsTokenValid {

        @Test
        @DisplayName("should return true for valid non-expired token")
        void shouldReturnTrueForValidToken() {
            String token = jwtService.generateToken("test@example.com", "Test", false);

            assertThat(jwtService.isTokenValid(token)).isTrue();
        }

        @Test
        @DisplayName("should return false for invalid token format")
        void shouldReturnFalseForInvalidFormat() {
            assertThat(jwtService.isTokenValid("not.a.valid.jwt")).isFalse();
        }

        @Test
        @DisplayName("should return false for empty token")
        void shouldReturnFalseForEmptyToken() {
            assertThat(jwtService.isTokenValid("")).isFalse();
        }

        @Test
        @DisplayName("should return false for null token")
        void shouldReturnFalseForNullToken() {
            assertThat(jwtService.isTokenValid(null)).isFalse();
        }

        @Test
        @DisplayName("should return false for token signed with different key")
        void shouldReturnFalseForTokenWithDifferentKey() {
            // The key is built once at startup, so sign with a second service instead of swapping the secret
            JwtService otherService = newService("different-secret-key-also-32-chars-minimum", new MockEnvironment());
            otherService.init();
            String token = otherService.generateToken("test@example.com", "Test", false);

            assertThat(jwtService.isTokenValid(token)).isFalse();
        }

        @Test
        @DisplayName("should return false for expired token")
        void shouldReturnFalseForExpiredToken() {
            // Set expiration to -1 (already expired)
            ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L);
            String token = jwtService.generateToken("test@example.com", "Test", false);

            // Reset expiration for validation
            ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION);

            assertThat(jwtService.isTokenValid(token)).isFalse();
        }
    }

    @Nested
    @DisplayName("isTokenValid logging")
    class IsTokenValidLogging {

        private final Logger jwtLogger = (Logger) LoggerFactory.getLogger(JwtService.class);
        private final ListAppender<ILoggingEvent> logEvents = new ListAppender<>();
        private Level previousLevel;

        @BeforeEach
        void attachAppender() {
            previousLevel = jwtLogger.getLevel();
            jwtLogger.setLevel(Level.DEBUG);
            logEvents.start();
            jwtLogger.addAppender(logEvents);
        }

        @AfterEach
        void detachAppender() {
            jwtLogger.detachAppender(logEvents);
            jwtLogger.setLevel(previousLevel);
        }

        private String tokenSignedWith(String secret, Date expiration) {
            return Jwts.builder()
                    .subject("admin@test.com")
                    .expiration(expiration)
                    .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                    .compact();
        }

        @Test
        @DisplayName("forged signature leaves one INFO line with the exception class and no token")
        void forgedSignatureIsInfo() {
            String forged = tokenSignedWith("another-secret-key-that-is-at-least-32-bytes-long",
                    new Date(System.currentTimeMillis() + 60_000));

            assertThat(jwtService.isTokenValid(forged)).isFalse();

            assertThat(logEvents.list).singleElement().satisfies(event -> {
                assertThat(event.getLevel()).isEqualTo(Level.INFO);
                assertThat(event.getFormattedMessage()).contains("SignatureException").doesNotContain(forged);
            });
        }

        @Test
        @DisplayName("expired token leaves one DEBUG line")
        void expiredIsDebug() {
            String expired = tokenSignedWith(SECRET_KEY, new Date(System.currentTimeMillis() - 60_000));

            assertThat(jwtService.isTokenValid(expired)).isFalse();

            assertThat(logEvents.list).singleElement().satisfies(event -> {
                assertThat(event.getLevel()).isEqualTo(Level.DEBUG);
                assertThat(event.getFormattedMessage()).contains("ExpiredJwtException").doesNotContain(expired);
            });
        }

        @Test
        @DisplayName("malformed token leaves one DEBUG line")
        void malformedIsDebug() {
            assertThat(jwtService.isTokenValid("not.a.valid.jwt")).isFalse();

            assertThat(logEvents.list).singleElement()
                    .satisfies(event -> assertThat(event.getLevel()).isEqualTo(Level.DEBUG));
        }
    }

    @Nested
    @DisplayName("extractName")
    class ExtractName {

        @Test
        @DisplayName("should extract name from token")
        void shouldExtractName() {
            String name = "John Doe";
            String token = jwtService.generateToken("john@example.com", name, false);

            assertThat(jwtService.extractName(token)).isEqualTo(name);
        }
    }

    @Nested
    @DisplayName("extractIsAdmin")
    class ExtractIsAdmin {

        @Test
        @DisplayName("should return true when user is admin")
        void shouldReturnTrueForAdmin() {
            String token = jwtService.generateToken("admin@example.com", "Admin", true);

            assertThat(jwtService.extractIsAdmin(token)).isTrue();
        }

        @Test
        @DisplayName("should return false when user is not admin")
        void shouldReturnFalseForNonAdmin() {
            String token = jwtService.generateToken("user@example.com", "User", false);

            assertThat(jwtService.extractIsAdmin(token)).isFalse();
        }

        @Test
        @DisplayName("should return false when the isAdmin claim is missing")
        void shouldReturnFalseWhenClaimMissing() {
            String token = jwtService.generateToken(Map.of("name", "No Claim"), "user@example.com");

            assertThat(jwtService.extractIsAdmin(token)).isFalse();
        }
    }
}
