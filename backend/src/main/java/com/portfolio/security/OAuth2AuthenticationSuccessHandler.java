package com.portfolio.security;

import com.portfolio.entity.User;
import com.portfolio.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserService userService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();
        String provider = oauthToken.getAuthorizedClientRegistrationId();
        String email = oAuth2User.getAttribute("email");

        // The allowlist matches on the e-mail string, which Google may return unverified
        if (!Boolean.TRUE.equals(oAuth2User.getAttribute("email_verified"))) {
            log.warn("Rejected admin login (unverified e-mail): {}", email);
            response.sendRedirect(frontendUrl + "/admin/login?error=not_authorized");
            return;
        }

        Optional<User> admin = userService.processOAuthUser(
                email,
                oAuth2User.getAttribute("name"),
                oAuth2User.getAttribute("picture"),
                provider,
                oAuth2User.getAttribute("sub"));

        if (admin.isEmpty()) {
            log.warn("Rejected admin login: {}", email);
            response.sendRedirect(frontendUrl + "/admin/login?error=not_authorized");
            return;
        }
        User user = admin.get();

        // Generate JWT token
        String jwt = jwtService.generateToken(user.getEmail(), user.getName(), user.getIsAdmin());

        // Set JWT as HTTP-only cookie with SameSite attribute
        // Use Secure flag when frontend URL is HTTPS (handles reverse proxy/tunnel scenarios)
        boolean isSecure = frontendUrl.startsWith("https://") || request.isSecure();
        ResponseCookie cookie = ResponseCookie.from("auth_token", jwt)
                .httpOnly(true)
                .secure(isSecure)
                .path("/")
                .maxAge(jwtService.getExpiration())
                .sameSite("Lax")  // Lax allows the cookie on OAuth redirects
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        log.info("Admin login: {}", user.getEmail());
        // Redirect to admin dashboard (cookie-based auth, no token in URL)
        response.sendRedirect(frontendUrl + "/admin");
    }
}
