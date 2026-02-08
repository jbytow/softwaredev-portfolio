package com.portfolio.config;

import com.portfolio.security.JwtAuthenticationFilter;
import com.portfolio.security.JwtService;
import com.portfolio.security.OAuth2AuthenticationSuccessHandler;
import com.portfolio.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;

/**
 * Test security configuration for @WebMvcTest controller tests.
 * Provides a simplified security setup without OAuth2 client dependencies.
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/admin/**").authenticated()
                        .anyRequest().permitAll());
        return http.build();
    }

    /**
     * No-op JWT filter for tests - passes through without JWT validation.
     */
    @Bean
    @Primary
    public JwtAuthenticationFilter testJwtAuthenticationFilter(JwtService jwtService) {
        return new JwtAuthenticationFilter(jwtService) {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                filterChain.doFilter(request, response);
            }
        };
    }

    /**
     * Mock OAuth2 success handler for tests.
     */
    @Bean
    @Primary
    public OAuth2AuthenticationSuccessHandler testOAuth2SuccessHandler(JwtService jwtService, UserService userService) {
        return new OAuth2AuthenticationSuccessHandler(jwtService, userService);
    }
}
