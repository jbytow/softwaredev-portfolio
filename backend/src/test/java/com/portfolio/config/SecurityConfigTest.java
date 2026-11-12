package com.portfolio.config;

import com.portfolio.controller.AdminPostController;
import com.portfolio.controller.AdminRpgStatController;
import com.portfolio.controller.AuthController;
import com.portfolio.controller.ContactController;
import com.portfolio.controller.PostController;
import com.portfolio.repository.UserRepository;
import com.portfolio.security.JwtAuthenticationFilter;
import com.portfolio.security.JwtService;
import com.portfolio.security.OAuth2AuthenticationSuccessHandler;
import com.portfolio.service.ContactService;
import com.portfolio.service.PostService;
import com.portfolio.service.RpgStatService;
import com.portfolio.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.Cookie.SameSite;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.CookieSameSiteSupplier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Runs the production SecurityConfig, JWT filter and allowlist (no TestSecurityConfig),
 * so a regression in the real chain fails here instead of passing against a test copy.
 */
@WebMvcTest(
        controllers = {AdminPostController.class, AdminRpgStatController.class, AuthController.class,
                PostController.class, ContactController.class},
        properties = {
                "spring.security.oauth2.client.registration.google.client-id=test-client-id",
                "spring.security.oauth2.client.registration.google.client-secret=test-client-secret"
        }
)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtService.class,
        OAuth2AuthenticationSuccessHandler.class, UserService.class})
@ActiveProfiles("test")
@DisplayName("SecurityConfig (real filter chain)")
class SecurityConfigTest {

    private static final String ADMIN_EMAIL = "admin@test.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @MockBean
    private PostService postService;

    @MockBean
    private RpgStatService rpgStatService;

    @MockBean
    private ContactService contactService;

    @MockBean
    private UserRepository userRepository;

    private Cookie authCookie(String token) {
        return new Cookie("auth_token", token);
    }

    private String tokenSignedWith(String secret, String email, boolean isAdmin, Date expiration) {
        return Jwts.builder()
                .subject(email)
                .claim("name", "Someone")
                .claim("isAdmin", isAdmin)
                .issuedAt(new Date(System.currentTimeMillis() - 120_000))
                .expiration(expiration)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    @Nested
    @DisplayName("anonymous API calls")
    class Anonymous {

        @Test
        @DisplayName("GET /api/admin/auth/me without a cookie returns 401, not a redirect to Google")
        void meWithoutCookieIs401() throws Exception {
            mockMvc.perform(get("/api/admin/auth/me").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().doesNotExist(HttpHeaders.LOCATION));
        }

        @Test
        @DisplayName("GET /api/admin/posts without a cookie returns 401")
        void adminPostsWithoutCookieIs401() throws Exception {
            mockMvc.perform(get("/api/admin/posts"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().doesNotExist(HttpHeaders.LOCATION));
        }

        @Test
        @DisplayName("GET /api/admin/rpg-stats without a cookie returns 401")
        void adminRpgStatsWithoutCookieIs401() throws Exception {
            mockMvc.perform(get("/api/admin/rpg-stats"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().doesNotExist(HttpHeaders.LOCATION));
        }

        @Test
        @DisplayName("POST /api/admin/auth/logout without a cookie succeeds and clears the cookie")
        void logoutWithoutCookieSucceeds() throws Exception {
            mockMvc.perform(post("/api/admin/auth/logout"))
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, startsWith("auth_token=;")));
        }

        @Test
        @DisplayName("GET /oauth2/authorization/google still redirects the browser to Google")
        void loginEntryStillRedirectsToGoogle() throws Exception {
            mockMvc.perform(get("/oauth2/authorization/google"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(header().string(HttpHeaders.LOCATION, startsWith("https://accounts.google.com/")));
        }

        @Test
        @DisplayName("GET /api/posts is public")
        void publicPostsArePublic() throws Exception {
            mockMvc.perform(get("/api/posts"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /api/contact is public")
        void contactIsPublic() throws Exception {
            mockMvc.perform(post("/api/contact")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"Jan","email":"jan@example.com","subject":"Hi","message":"Hello"}
                                    """))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("admin role")
    class AdminRole {

        @Test
        @DisplayName("token issued to an e-mail later removed from the allowlist gets 403")
        void delistedAdminTokenIs403() throws Exception {
            String token = jwtService.generateToken("former@test.com", "Former", true);

            mockMvc.perform(get("/api/admin/posts").cookie(authCookie(token)))
                    .andExpect(status().isForbidden());
            mockMvc.perform(get("/api/admin/rpg-stats").cookie(authCookie(token)))
                    .andExpect(status().isForbidden());
            mockMvc.perform(get("/api/admin/auth/me").cookie(authCookie(token)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("authenticated principal without ROLE_ADMIN (isAdmin=false) gets 403")
        void nonAdminTokenIs403() throws Exception {
            String token = jwtService.generateToken(ADMIN_EMAIL, "Admin", false);

            mockMvc.perform(get("/api/admin/posts").cookie(authCookie(token)))
                    .andExpect(status().isForbidden());
            mockMvc.perform(get("/api/admin/rpg-stats").cookie(authCookie(token)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("token without the isAdmin claim gets 403, not a server error")
        void tokenWithoutIsAdminClaimIs403() throws Exception {
            String token = jwtService.generateToken(Map.of("name", "No Claim"), ADMIN_EMAIL);

            mockMvc.perform(get("/api/admin/posts").cookie(authCookie(token)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("allowlisted admin reaches an RPG admin route")
        void adminReachesRpgAdminRoute() throws Exception {
            String token = jwtService.generateToken(ADMIN_EMAIL, "Admin", true);

            mockMvc.perform(get("/api/admin/rpg-stats").cookie(authCookie(token)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("OAuth login outcome")
    class OAuthLogin {

        private final Logger configLogger = (Logger) LoggerFactory.getLogger(SecurityConfig.class);
        private final ListAppender<ILoggingEvent> logEvents = new ListAppender<>();

        @BeforeEach
        void attachAppender() {
            logEvents.start();
            configLogger.addAppender(logEvents);
        }

        @AfterEach
        void detachAppender() {
            configLogger.detachAppender(logEvents);
        }

        @Test
        @DisplayName("callback with an unknown state redirects to the SPA login page with oauth_failed")
        void unknownStateRedirectsToSpaLogin() throws Exception {
            mockMvc.perform(get("/login/oauth2/code/google")
                            .param("code", "secret-auth-code")
                            .param("state", "unknown"))
                    .andExpect(status().isFound())
                    .andExpect(header().string(HttpHeaders.LOCATION,
                            "http://localhost:3000/admin/login?error=oauth_failed"));

            assertThat(logEvents.list)
                    .filteredOn(event -> event.getLevel() == Level.WARN)
                    .singleElement()
                    .satisfies(event -> assertThat(event.getFormattedMessage())
                            .contains("OAuth2AuthenticationException")
                            .doesNotContain("secret-auth-code"));
        }

        @Test
        @DisplayName("callback after the user cancelled consent redirects to the SPA login page")
        void cancelledConsentRedirectsToSpaLogin() throws Exception {
            mockMvc.perform(get("/login/oauth2/code/google")
                            .param("error", "access_denied")
                            .param("state", "x"))
                    .andExpect(status().isFound())
                    .andExpect(header().string(HttpHeaders.LOCATION,
                            "http://localhost:3000/admin/login?error=oauth_failed"));
        }

        @Test
        @DisplayName("the wired success handler rejects an allowlisted but unverified Google e-mail")
        void unverifiedEmailRedirectsToNotAuthorized() throws Exception {
            Map<String, Object> attributes = Map.of("sub", "google-sub-1", "email", ADMIN_EMAIL,
                    "name", "Admin", "email_verified", false);
            DefaultOAuth2User user = new DefaultOAuth2User(
                    List.of(new OAuth2UserAuthority(attributes)), attributes, "email");
            MockHttpServletResponse response = new MockHttpServletResponse();

            oAuth2SuccessHandler.onAuthenticationSuccess(new MockHttpServletRequest(), response,
                    new OAuth2AuthenticationToken(user, user.getAuthorities(), "google"));

            assertThat(response.getRedirectedUrl())
                    .isEqualTo("http://localhost:3000/admin/login?error=not_authorized");
            assertThat(response.getHeaders(HttpHeaders.SET_COOKIE)).isEmpty();
            verifyNoInteractions(userRepository);
        }
    }

    @Nested
    @DisplayName("JWT cookie and header")
    class Jwt {

        @Test
        @DisplayName("valid admin JWT in the auth_token cookie reaches /api/admin/posts")
        void adminCookieIsAccepted() throws Exception {
            String token = jwtService.generateToken(ADMIN_EMAIL, "Admin", true);

            mockMvc.perform(get("/api/admin/posts").cookie(authCookie(token)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("valid admin JWT as a Bearer header reaches /api/admin/posts")
        void adminBearerHeaderIsAccepted() throws Exception {
            String token = jwtService.generateToken(ADMIN_EMAIL, "Admin", true);

            mockMvc.perform(get("/api/admin/posts").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("JWT signed with another key is rejected with 401")
        void forgedTokenIs401() throws Exception {
            String forged = tokenSignedWith("another-secret-key-that-is-at-least-32-bytes-long",
                    ADMIN_EMAIL, true, new Date(System.currentTimeMillis() + 60_000));

            mockMvc.perform(get("/api/admin/posts").cookie(authCookie(forged)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("expired JWT is rejected with 401")
        void expiredTokenIs401() throws Exception {
            String expired = tokenSignedWith(jwtSecret, ADMIN_EMAIL, true,
                    new Date(System.currentTimeMillis() - 60_000));

            mockMvc.perform(get("/api/admin/posts").cookie(authCookie(expired)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("admin writes from another origin")
    class CrossOriginAdminWrites {

        private static final String EVIL_ORIGIN = "https://evil.example";
        private static final String FRONTEND_ORIGIN = "http://localhost:3000";

        private String adminToken() {
            return jwtService.generateToken(ADMIN_EMAIL, "Admin", true);
        }

        @Test
        @DisplayName("POST /api/admin/posts with a foreign Origin is rejected with 403 and an ApiResponse body")
        void foreignOriginWriteIs403() throws Exception {
            mockMvc.perform(post("/api/admin/posts")
                            .cookie(authCookie(adminToken()))
                            .header(HttpHeaders.ORIGIN, EVIL_ORIGIN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").exists());

            verifyNoInteractions(postService);
        }

        @Test
        @DisplayName("DELETE /api/admin/rpg-stats/{id} with a foreign Origin is rejected with 403")
        void foreignOriginRpgWriteIs403() throws Exception {
            mockMvc.perform(delete("/api/admin/rpg-stats/{id}", UUID.randomUUID())
                            .cookie(authCookie(adminToken()))
                            .header(HttpHeaders.ORIGIN, EVIL_ORIGIN))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));

            verifyNoInteractions(rpgStatService);
        }

        @Test
        @DisplayName("DELETE /api/admin/posts/{id} with a foreign Referer and no Origin is rejected with 403")
        void foreignRefererWriteIs403() throws Exception {
            mockMvc.perform(delete("/api/admin/posts/{id}", UUID.randomUUID())
                            .cookie(authCookie(adminToken()))
                            .header(HttpHeaders.REFERER, EVIL_ORIGIN + "/attack.html"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));

            verifyNoInteractions(postService);
        }

        @Test
        @DisplayName("POST /api/admin/posts from the frontend origin passes the check (400 from validation)")
        void frontendOriginWritePasses() throws Exception {
            mockMvc.perform(post("/api/admin/posts")
                            .cookie(authCookie(adminToken()))
                            .header(HttpHeaders.ORIGIN, FRONTEND_ORIGIN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("DELETE /api/admin/rpg-stats/{id} from the frontend origin passes the check")
        void frontendOriginRpgWritePasses() throws Exception {
            mockMvc.perform(delete("/api/admin/rpg-stats/{id}", UUID.randomUUID())
                            .cookie(authCookie(adminToken()))
                            .header(HttpHeaders.ORIGIN, FRONTEND_ORIGIN))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("DELETE /api/admin/posts/{id} with the frontend Referer passes the check")
        void frontendRefererWritePasses() throws Exception {
            mockMvc.perform(delete("/api/admin/posts/{id}", UUID.randomUUID())
                            .cookie(authCookie(adminToken()))
                            .header(HttpHeaders.REFERER, FRONTEND_ORIGIN + "/admin/posts"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /api/admin/posts without Origin and Referer (non-browser client) passes the check")
        void writeWithoutOriginPasses() throws Exception {
            mockMvc.perform(post("/api/admin/posts")
                            .cookie(authCookie(adminToken()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("GET /api/admin/posts with a foreign Referer is not blocked: reads change nothing")
        void foreignReadIsAllowed() throws Exception {
            mockMvc.perform(get("/api/admin/posts")
                            .cookie(authCookie(adminToken()))
                            .header(HttpHeaders.REFERER, EVIL_ORIGIN + "/page"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /api/admin/auth/logout with a foreign Origin is rejected too: it is a state change")
        void foreignLogoutIs403() throws Exception {
            mockMvc.perform(post("/api/admin/auth/logout")
                            .cookie(authCookie(adminToken()))
                            .header(HttpHeaders.ORIGIN, EVIL_ORIGIN))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
        }

        @Test
        @DisplayName("public POST /api/contact is not subject to the admin origin check")
        void publicWriteIsNotChecked() throws Exception {
            mockMvc.perform(post("/api/contact")
                            .header(HttpHeaders.REFERER, EVIL_ORIGIN + "/page")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"Jan","email":"jan@example.com","subject":"Hi","message":"Hello"}
                                    """))
                    .andExpect(status().isOk());
        }
    }

    private static void assertSessionCookieSecure(ApplicationContext context, boolean expected) throws Exception {
        assertThat(context.containsBean("sessionCookieSecureInitializer")).isTrue();
        MockServletContext servletContext = new MockServletContext();
        context.getBean("sessionCookieSecureInitializer", ServletContextInitializer.class).onStartup(servletContext);
        assertThat(servletContext.getSessionCookieConfig().isSecure()).isEqualTo(expected);
    }

    @Nested
    @DisplayName("JSESSIONID used for the OAuth state")
    class SessionCookie {

        @Autowired
        private ApplicationContext context;

        @Test
        @DisplayName("gets SameSite=Lax, other cookies are left alone")
        void sameSiteLaxForSessionCookieOnly() {
            assertThat(context.getBeansOfType(CookieSameSiteSupplier.class).values()).anySatisfy(supplier -> {
                assertThat(supplier.getSameSite(new Cookie("JSESSIONID", "x"))).isEqualTo(SameSite.LAX);
                assertThat(supplier.getSameSite(new Cookie("other", "x"))).isNull();
            });
        }

        @Test
        @DisplayName("is not marked Secure for an http frontend")
        void notSecureForHttpFrontend() throws Exception {
            assertSessionCookieSecure(context, false);
        }
    }

    @Nested
    @TestPropertySource(properties = "app.frontend.url=https://jbytow.pl")
    @DisplayName("JSESSIONID behind an https frontend")
    class HttpsSessionCookie {

        @Autowired
        private ApplicationContext context;

        @Test
        @DisplayName("is marked Secure although Tomcat itself only sees plain http")
        void secureForHttpsFrontend() throws Exception {
            assertSessionCookieSecure(context, true);
        }
    }
}
