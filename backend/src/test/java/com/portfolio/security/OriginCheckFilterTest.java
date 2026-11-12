package com.portfolio.security;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OriginCheckFilter")
class OriginCheckFilterTest {

    private static final String FRONTEND_URL = "https://jbytow.pl";

    private final Logger filterLogger = (Logger) LoggerFactory.getLogger(OriginCheckFilter.class);
    private final ListAppender<ILoggingEvent> logEvents = new ListAppender<>();

    @BeforeEach
    void attachAppender() {
        logEvents.start();
        filterLogger.addAppender(logEvents);
    }

    @AfterEach
    void detachAppender() {
        filterLogger.detachAppender(logEvents);
    }

    private static MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        return request;
    }

    private static MockHttpServletResponse filter(String frontendUrl, MockHttpServletRequest request,
                                                  MockFilterChain chain) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        new OriginCheckFilter(frontendUrl, new ObjectMapper()).doFilter(request, response, chain);
        return response;
    }

    // MockFilterChain records the request only when the filter let it through
    private static boolean passes(String frontendUrl, MockHttpServletRequest request) throws Exception {
        MockFilterChain chain = new MockFilterChain();
        filter(frontendUrl, request, chain);
        return chain.getRequest() != null;
    }

    private static MockHttpServletRequest adminWrite(String header, String value) {
        MockHttpServletRequest request = request("POST", "/api/admin/posts");
        request.addHeader(header, value);
        return request;
    }

    @Test
    @DisplayName("a foreign Origin on an admin write gets 403 with an ApiResponse body and one WARN line")
    void foreignOriginIsRejected() throws Exception {
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletResponse response = filter(FRONTEND_URL,
                adminWrite(HttpHeaders.ORIGIN, "https://evil.example"), chain);

        assertThat(chain.getRequest()).isNull();
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getContentAsString()).contains("\"success\":false").contains("\"message\":");
        assertThat(logEvents.list)
                .filteredOn(event -> event.getLevel() == Level.WARN)
                .singleElement()
                .satisfies(event -> assertThat(event.getFormattedMessage())
                        .contains("POST")
                        .contains("/api/admin/posts")
                        .contains("https://evil.example"));
    }

    @ParameterizedTest(name = "Origin {0} matches")
    @ValueSource(strings = {"https://jbytow.pl", "HTTPS://Jbytow.PL", "https://jbytow.pl:443"})
    @DisplayName("an Origin equal to the frontend (case and default port ignored) passes")
    void matchingOriginPasses(String origin) throws Exception {
        assertThat(passes(FRONTEND_URL, adminWrite(HttpHeaders.ORIGIN, origin))).isTrue();
        assertThat(logEvents.list).isEmpty();
    }

    @ParameterizedTest(name = "Origin {0} is foreign")
    @ValueSource(strings = {
            "https://evil.example",
            "http://jbytow.pl",
            "https://jbytow.pl:8443",
            "https://sub.jbytow.pl",
            "https://jbytow.pl.evil.example",
            "null",
            "not a url"
    })
    @DisplayName("an Origin differing in scheme, host or port, or unparseable, is rejected")
    void foreignOriginVariantsAreRejected(String origin) throws Exception {
        assertThat(passes(FRONTEND_URL, adminWrite(HttpHeaders.ORIGIN, origin))).isFalse();
    }

    @Test
    @DisplayName("a frontend URL with a trailing slash still matches its Origin")
    void trailingSlashInFrontendUrl() throws Exception {
        assertThat(passes("https://jbytow.pl/", adminWrite(HttpHeaders.ORIGIN, "https://jbytow.pl"))).isTrue();
    }

    @Test
    @DisplayName("a non-default port in the frontend URL must match exactly")
    void explicitPortMustMatch() throws Exception {
        assertThat(passes("http://localhost:5173", adminWrite(HttpHeaders.ORIGIN, "http://localhost:5173"))).isTrue();
        assertThat(passes("http://localhost:5173", adminWrite(HttpHeaders.ORIGIN, "http://localhost"))).isFalse();
        assertThat(passes("http://localhost:5173", adminWrite(HttpHeaders.ORIGIN, "http://localhost:3000"))).isFalse();
    }

    @ParameterizedTest(name = "Referer {0} matches")
    @ValueSource(strings = {
            "https://jbytow.pl/admin/posts",
            "https://jbytow.pl/admin/posts?page=2#top",
            "https://JBYTOW.pl/",
            "https://jbytow.pl"
    })
    @DisplayName("without Origin, a Referer from the frontend (any path) passes")
    void matchingRefererPasses(String referer) throws Exception {
        assertThat(passes(FRONTEND_URL, adminWrite(HttpHeaders.REFERER, referer))).isTrue();
    }

    @ParameterizedTest(name = "Referer {0} is foreign")
    @ValueSource(strings = {
            "https://evil.example/https://jbytow.pl/admin",
            "https://jbytow.pl.evil.example/admin",
            "https://jbytow.pl@evil.example/admin",
            "https://jbytow.pl:8443/admin",
            "not a url"
    })
    @DisplayName("without Origin, a Referer from another origin is rejected")
    void foreignRefererIsRejected(String referer) throws Exception {
        assertThat(passes(FRONTEND_URL, adminWrite(HttpHeaders.REFERER, referer))).isFalse();
    }

    @Test
    @DisplayName("Origin wins over Referer when both are present")
    void originTakesPrecedence() throws Exception {
        MockHttpServletRequest request = adminWrite(HttpHeaders.ORIGIN, "https://evil.example");
        request.addHeader(HttpHeaders.REFERER, "https://jbytow.pl/admin");

        assertThat(passes(FRONTEND_URL, request)).isFalse();
    }

    @Test
    @DisplayName("a write without Origin and Referer (non-browser client) passes")
    void noHeadersPass() throws Exception {
        assertThat(passes(FRONTEND_URL, request("POST", "/api/admin/posts"))).isTrue();
    }

    @ParameterizedTest(name = "{0} passes")
    @ValueSource(strings = {"GET", "HEAD", "OPTIONS"})
    @DisplayName("safe methods are not checked")
    void safeMethodsPass(String method) throws Exception {
        MockHttpServletRequest request = request(method, "/api/admin/posts");
        request.addHeader(HttpHeaders.ORIGIN, "https://evil.example");

        assertThat(passes(FRONTEND_URL, request)).isTrue();
    }

    @ParameterizedTest(name = "{0} is checked")
    @ValueSource(strings = {"POST", "PUT", "PATCH", "DELETE"})
    @DisplayName("every state-changing method on /api/admin/** is checked")
    void writeMethodsAreChecked(String method) throws Exception {
        MockHttpServletRequest request = request(method, "/api/admin/posts/1");
        request.addHeader(HttpHeaders.ORIGIN, "https://evil.example");

        assertThat(passes(FRONTEND_URL, request)).isFalse();
    }

    @ParameterizedTest(name = "{0} is not checked")
    @ValueSource(strings = {"/api/contact", "/api/administrator", "/api/posts"})
    @DisplayName("writes outside /api/admin/** are not checked")
    void otherPathsPass(String path) throws Exception {
        MockHttpServletRequest request = request("POST", path);
        request.addHeader(HttpHeaders.ORIGIN, "https://evil.example");

        assertThat(passes(FRONTEND_URL, request)).isTrue();
    }
}
