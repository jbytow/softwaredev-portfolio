package com.portfolio.service;

import com.portfolio.entity.User;
import com.portfolio.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "adminEmails", " Admin@Test.com , second@test.com");
    }

    @Nested
    @DisplayName("isAdmin")
    class IsAdmin {

        @Test
        @DisplayName("matches an allowlist entry regardless of surrounding spaces and case")
        void matchesTrimmedCaseInsensitiveEntry() {
            assertThat(userService.isAdmin("ADMIN@test.com")).isTrue();
            assertThat(userService.isAdmin("admin@test.com")).isTrue();
            assertThat(userService.isAdmin("second@test.com")).isTrue();
        }

        @Test
        @DisplayName("rejects an e-mail that is not on the allowlist")
        void rejectsUnknownEmail() {
            assertThat(userService.isAdmin("someone@test.com")).isFalse();
        }

        @Test
        @DisplayName("does not match on a prefix or suffix of an allowlisted e-mail")
        void rejectsPartialMatch() {
            assertThat(userService.isAdmin("admin@test.com.evil.example")).isFalse();
            assertThat(userService.isAdmin("xadmin@test.com")).isFalse();
        }
    }

    @Nested
    @DisplayName("processOAuthUser")
    class ProcessOAuthUser {

        @Test
        @DisplayName("creates an allowlisted new user as admin")
        void newAllowlistedUserIsAdmin() {
            given(userRepository.findByEmail("ADMIN@test.com")).willReturn(Optional.empty());
            given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

            Optional<User> user = userService.processOAuthUser("ADMIN@test.com", "Admin", null, "google", "sub-1");

            assertThat(user).hasValueSatisfying(saved -> {
                assertThat(saved.getIsAdmin()).isTrue();
                assertThat(saved.getEmail()).isEqualTo("ADMIN@test.com");
                assertThat(saved.getProviderId()).isEqualTo("sub-1");
            });
        }

        @Test
        @DisplayName("a non-allowlisted e-mail is not saved: the repository is not touched at all")
        void unknownEmailIsNotSaved() {
            Optional<User> result = userService.processOAuthUser("someone@test.com", "Someone", "https://avatar", "google", "sub-2");

            assertThat(result).isEmpty();
            verify(userRepository, never()).findByEmail(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("a former admin removed from the allowlist is rejected without updating the stored row")
        void delistedAdminIsNotUpdated() {
            Optional<User> result = userService.processOAuthUser("former@test.com", "Former", null, "google", "sub-3");

            assertThat(result).isEmpty();
            verifyNoInteractions(userRepository);
        }

        @Test
        @DisplayName("an allowlisted e-mail that is already stored is updated, not duplicated")
        void existingAdminIsUpdated() {
            User stored = User.builder().email("admin@test.com").name("Old").isAdmin(true).build();
            given(userRepository.findByEmail("admin@test.com")).willReturn(Optional.of(stored));
            given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

            Optional<User> result = userService.processOAuthUser("admin@test.com", "New Name", "https://avatar", "google", "sub-1");

            assertThat(result).containsSame(stored);
            verify(userRepository).save(stored);
            assertThat(stored.getName()).isEqualTo("New Name");
            assertThat(stored.getAvatarUrl()).isEqualTo("https://avatar");
            assertThat(stored.getLastLoginAt()).isNotNull();
        }
    }
}
