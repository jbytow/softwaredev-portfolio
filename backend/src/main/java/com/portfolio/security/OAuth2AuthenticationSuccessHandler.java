package com.portfolio.security;

import com.portfolio.entity.User;
import com.portfolio.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
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

        String email = extractEmail(oAuth2User, provider);
        String name = extractName(oAuth2User, provider);
        String avatarUrl = extractAvatarUrl(oAuth2User, provider);
        String providerId = extractProviderId(oAuth2User, provider);

        // Create or update user and check if admin
        User user = userService.processOAuthUser(email, name, avatarUrl, provider, providerId);

        if (!user.getIsAdmin()) {
            // User is not an admin, redirect with error
            response.sendRedirect(frontendUrl + "/admin/login?error=not_authorized");
            return;
        }

        // Generate JWT token
        String jwt = jwtService.generateToken(user.getEmail(), user.getName(), user.getIsAdmin());

        // Set JWT as HTTP-only cookie with SameSite attribute
        // Use Secure flag when frontend URL is HTTPS (handles reverse proxy/tunnel scenarios)
        boolean isSecure = frontendUrl.startsWith("https://") || request.isSecure();
        ResponseCookie cookie = ResponseCookie.from("auth_token", jwt)
                .httpOnly(true)
                .secure(isSecure)
                .path("/")
                .maxAge(Duration.ofHours(24))
                .sameSite("Lax")  // Lax allows the cookie on OAuth redirects
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // Redirect to admin dashboard (cookie-based auth, no token in URL)
        response.sendRedirect(frontendUrl + "/admin");
    }

    private String extractEmail(OAuth2User oAuth2User, String provider) {
        if ("github".equals(provider)) {
            // GitHub might not provide email directly
            String email = oAuth2User.getAttribute("email");
            if (email == null) {
                // Use login as fallback
                String login = oAuth2User.getAttribute("login");
                return login + "@github.user";
            }
            return email;
        }
        return oAuth2User.getAttribute("email");
    }

    private String extractName(OAuth2User oAuth2User, String provider) {
        if ("github".equals(provider)) {
            String name = oAuth2User.getAttribute("name");
            return name != null ? name : oAuth2User.getAttribute("login");
        }
        return oAuth2User.getAttribute("name");
    }

    private String extractAvatarUrl(OAuth2User oAuth2User, String provider) {
        if ("github".equals(provider)) {
            return oAuth2User.getAttribute("avatar_url");
        }
        return oAuth2User.getAttribute("picture");
    }

    private String extractProviderId(OAuth2User oAuth2User, String provider) {
        if ("github".equals(provider)) {
            Integer id = oAuth2User.getAttribute("id");
            return id != null ? id.toString() : null;
        }
        return oAuth2User.getAttribute("sub");
    }
}
