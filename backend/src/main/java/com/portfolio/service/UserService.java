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
        return getAdminEmails().contains(email.toLowerCase());
    }

    @Transactional
    public User processOAuthUser(String email, String name, String avatarUrl, String provider, String providerId) {
        Optional<User> existingUser = userRepository.findByEmail(email);

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            user.setName(name);
            user.setAvatarUrl(avatarUrl);
            user.setLastLoginAt(OffsetDateTime.now());
        } else {
            user = User.builder()
                    .email(email)
                    .name(name)
                    .avatarUrl(avatarUrl)
                    .provider(provider)
                    .providerId(providerId)
                    .isAdmin(isAdmin(email))
                    .build();
        }

        // Always check admin status in case whitelist changed
        user.setIsAdmin(isAdmin(email));

        return userRepository.save(user);
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
