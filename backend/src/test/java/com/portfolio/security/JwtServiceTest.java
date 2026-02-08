package com.portfolio.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtService")
class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET_KEY = "test-secret-key-minimum-32-characters-long-for-testing";
    private static final long EXPIRATION = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION);
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
            // Create a token with current key
            String token = jwtService.generateToken("test@example.com", "Test", false);

            // Change the secret key
            ReflectionTestUtils.setField(jwtService, "secretKey",
                    "different-secret-key-also-32-chars-minimum");

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
    }
}
