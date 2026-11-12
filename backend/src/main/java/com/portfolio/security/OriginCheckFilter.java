package com.portfolio.security;

import tools.jackson.databind.ObjectMapper;
import com.portfolio.dto.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

/**
 * CSRF defence for the cookie-based admin session: SameSite=Lax does not stop pages on sibling
 * subdomains, so state-changing admin requests must come from the frontend origin. Requests with
 * neither Origin nor Referer are let through, because browsers always send one of them on
 * cross-site writes and non-browser clients carry no ambient cookie to abuse.
 */
@Slf4j
public class OriginCheckFilter extends OncePerRequestFilter {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");
    private static final RequestMatcher ADMIN_PATHS = PathPatternRequestMatcher.pathPattern("/api/admin/**");

    private final String frontendOrigin;
    private final ObjectMapper objectMapper;

    public OriginCheckFilter(String frontendUrl, ObjectMapper objectMapper) {
        this.frontendOrigin = originOf(frontendUrl);
        if (frontendOrigin == null) {
            throw new IllegalArgumentException("app.frontend.url is not an absolute http(s) URL: " + frontendUrl);
        }
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String claimedOrigin = request.getHeader(HttpHeaders.ORIGIN);
        if (claimedOrigin == null) {
            claimedOrigin = request.getHeader(HttpHeaders.REFERER);
        }

        if (isAdminWrite(request) && claimedOrigin != null && !frontendOrigin.equals(originOf(claimedOrigin))) {
            log.warn("Rejected cross-origin {} {} from origin {}",
                    request.getMethod(), request.getRequestURI(), claimedOrigin);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            objectMapper.writeValue(response.getWriter(), ApiResponse.error("Request origin not allowed"));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private static boolean isAdminWrite(HttpServletRequest request) {
        return !SAFE_METHODS.contains(request.getMethod()) && ADMIN_PATHS.matches(request);
    }

    // Reduces an Origin or Referer to scheme://host:port so default ports, case and paths do not matter
    private static String originOf(String url) {
        try {
            URI uri = new URI(url.trim());
            if (uri.getScheme() == null || uri.getHost() == null) {
                return null;
            }
            String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
            int port = uri.getPort() != -1 ? uri.getPort() : defaultPort(scheme);
            return scheme + "://" + uri.getHost().toLowerCase(Locale.ROOT) + ":" + port;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private static int defaultPort(String scheme) {
        return switch (scheme) {
            case "http" -> 80;
            case "https" -> 443;
            default -> -1;
        };
    }
}
