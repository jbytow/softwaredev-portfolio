package com.portfolio.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.security.JwtAuthenticationFilter;
import com.portfolio.security.OAuth2AuthenticationSuccessHandler;
import com.portfolio.security.OriginCheckFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.CookieSameSiteSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Clearing the auth cookie must work even when the token has already expired
                        .requestMatchers(HttpMethod.POST, "/api/admin/auth/logout").permitAll()
                        // Admin endpoints require the allowlist-backed admin role, not just any token
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // Public by default on purpose (owner decision D5): only the admin API is protected
                        .anyRequest().permitAll())
                // Without this, oauth2Login makes every unauthenticated XHR a 302 to Google
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/api/**")))
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler)
                        // Spring's default /login?error is not a route of the SPA
                        .failureHandler((request, response, exception) -> {
                            log.warn("OAuth login failed: {}", exception.getClass().getSimpleName());
                            response.sendRedirect(frontendUrl + "/admin/login?error=oauth_failed");
                        }))
                // Before CorsFilter, so a foreign Origin on an admin write is always answered and logged here
                .addFilterBefore(new OriginCheckFilter(frontendUrl, objectMapper), CorsFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // oauth2Login keeps the authorization request in the HTTP session, so JSESSIONID exists despite STATELESS
    @Bean
    public CookieSameSiteSupplier sessionCookieSameSiteSupplier() {
        return CookieSameSiteSupplier.ofLax().whenHasName("JSESSIONID");
    }

    // Behind the tunnel Tomcat only sees plain http and would never set Secure on its own;
    // same rule as the auth_token cookie
    @Bean
    public ServletContextInitializer sessionCookieSecureInitializer() {
        return servletContext -> {
            if (frontendUrl.startsWith("https://")) {
                servletContext.getSessionCookieConfig().setSecure(true);
            }
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Content-Type",
                "Authorization",
                "Accept",
                "Accept-Language",
                "X-Requested-With"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
