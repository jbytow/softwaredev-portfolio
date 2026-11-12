package com.portfolio.security;

import com.portfolio.service.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {

    private static final String SECRET_KEY = "test-secret-key-minimum-32-characters-long-for-testing";

    @Mock
    private UserService userService;

    private JwtService jwtService;
    private JwtAuthenticationFilter filter;
    private MockHttpServletResponse response;
    private MockFilterChain chain;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(new MockEnvironment());
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 60_000L);
        jwtService.init();
        filter = new JwtAuthenticationFilter(jwtService, userService);
        response = new MockHttpServletResponse();
        chain = new MockFilterChain();
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletRequest requestWithCookie(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/posts");
        request.setCookies(new Cookie("auth_token", token));
        return request;
    }

    private Authentication authentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean hasAuthority(Authentication auth, String authority) {
        return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(authority::equals);
    }

    @Test
    @DisplayName("grants ROLE_ADMIN to a valid admin token whose e-mail is on the allowlist")
    void grantsAdminForAllowlistedEmail() throws Exception {
        given(userService.isAdmin("admin@test.com")).willReturn(true);
        String token = jwtService.generateToken("admin@test.com", "Admin", true);

        filter.doFilter(requestWithCookie(token), response, chain);

        Authentication auth = authentication();
        assertThat(auth).isNotNull();
        assertThat(hasAuthority(auth, "ROLE_ADMIN")).isTrue();
        assertThat(((UserPrincipal) auth.getPrincipal()).isAdmin()).isTrue();
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    @DisplayName("reads the token from a Bearer header as well")
    void readsBearerHeader() throws Exception {
        given(userService.isAdmin("admin@test.com")).willReturn(true);
        String token = jwtService.generateToken("admin@test.com", "Admin", true);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/posts");
        request.addHeader("Authorization", "Bearer " + token);

        filter.doFilter(request, response, chain);

        assertThat(hasAuthority(authentication(), "ROLE_ADMIN")).isTrue();
    }

    @Test
    @DisplayName("does not grant ROLE_ADMIN when the e-mail has left the allowlist")
    void noAdminForDelistedEmail() throws Exception {
        given(userService.isAdmin("former@test.com")).willReturn(false);
        String token = jwtService.generateToken("former@test.com", "Former", true);

        filter.doFilter(requestWithCookie(token), response, chain);

        Authentication auth = authentication();
        assertThat(auth).isNotNull();
        assertThat(hasAuthority(auth, "ROLE_ADMIN")).isFalse();
        assertThat(((UserPrincipal) auth.getPrincipal()).isAdmin()).isFalse();
    }

    @Test
    @DisplayName("does not grant ROLE_ADMIN when the token says isAdmin=false, even for an allowlisted e-mail")
    void noAdminWhenClaimIsFalse() throws Exception {
        String token = jwtService.generateToken("admin@test.com", "Admin", false);

        filter.doFilter(requestWithCookie(token), response, chain);

        assertThat(hasAuthority(authentication(), "ROLE_ADMIN")).isFalse();
    }

    @Test
    @DisplayName("handles a token without the isAdmin claim without throwing")
    void tokenWithoutIsAdminClaim() {
        String token = jwtService.generateToken(Map.of("name", "No Claim"), "admin@test.com");

        assertThatCode(() -> filter.doFilter(requestWithCookie(token), response, chain))
                .doesNotThrowAnyException();
        assertThat(hasAuthority(authentication(), "ROLE_ADMIN")).isFalse();
    }

    @Test
    @DisplayName("leaves the request anonymous for an invalid token and continues the chain")
    void invalidTokenStaysAnonymous() throws Exception {
        filter.doFilter(requestWithCookie("not.a.valid.jwt"), response, chain);

        assertThat(authentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
        verifyNoInteractions(userService);
    }
}
