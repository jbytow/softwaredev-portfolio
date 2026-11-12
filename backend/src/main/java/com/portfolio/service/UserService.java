package com.portfolio.service;

import com.portfolio.dto.UserDto;
import com.portfolio.entity.User;
import com.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Value("${app.admin.emails}")
    private String adminEmails;

    public Set<String> getAdminEmails() {
        return Arrays.stream(adminEmails.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    public boolean isAdmin(String email) {
        return email != null && getAdminEmails().contains(email.toLowerCase());
    }

    /**
     * Upserts an allowlisted account and returns it; any other account is rejected (empty) before
     * the repository is touched, so strangers' e-mails, names and avatars are never stored.
     */
    @Transactional
    public Optional<User> processOAuthUser(String email, String name, String avatarUrl, String provider, String providerId) {
        if (!isAdmin(email)) {
            return Optional.empty();
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> User.builder()
                        .email(email)
                        .provider(provider)
                        .providerId(providerId)
                        .build());
        user.setName(name);
        user.setAvatarUrl(avatarUrl);
        user.setLastLoginAt(OffsetDateTime.now());
        user.setIsAdmin(true);

        return Optional.of(userRepository.save(user));
    }

    public Optional<UserDto> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::mapToDto);
    }

    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .avatarUrl(user.getAvatarUrl())
                .provider(user.getProvider())
                .isAdmin(user.getIsAdmin())
                .build();
    }
}
