package com.portfolio.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.portfolio.controller.PostController;
import com.portfolio.entity.Category;
import com.portfolio.security.JwtService;
import com.portfolio.service.PostService;
import com.portfolio.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.data.core.TypeInformation;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PostController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration.class
        }
)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    private final Logger handlerLogger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ListAppender<ILoggingEvent> logEvents = new ListAppender<>();

    @BeforeEach
    void attachLogAppender() {
        logEvents.start();
        handlerLogger.addAppender(logEvents);
    }

    @AfterEach
    void detachLogAppender() {
        handlerLogger.detachAppender(logEvents);
    }

    @Test
    @DisplayName("GET on an unknown path returns 404 and logs one WARN line without a stack trace")
    void unknownPathReturns404WithoutStackTrace() throws Exception {
        mockMvc.perform(get("/api/.env").header("CF-Connecting-IP", "203.0.113.7"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not found"));

        List<ILoggingEvent> events = logEvents.list;
        assertThat(events).hasSize(1);
        ILoggingEvent event = events.get(0);
        assertThat(event.getLevel()).isEqualTo(Level.WARN);
        assertThat(event.getThrowableProxy()).isNull();
        assertThat(event.getFormattedMessage())
                .contains("GET")
                .contains("/api/.env")
                .contains("203.0.113.7")
                .doesNotContain("Unexpected error");
    }

    @Test
    @DisplayName("client address comes from CF-Connecting-IP, not from a spoofed X-Forwarded-For")
    void clientAddressIgnoresSpoofedForwardedFor() throws Exception {
        mockMvc.perform(get("/api/.env")
                        .header("X-Forwarded-For", "8.8.8.8, 10.0.0.1")
                        .header("CF-Connecting-IP", "203.0.113.7"))
                .andExpect(status().isNotFound());

        assertThat(logEvents.list).hasSize(1);
        assertThat(logEvents.list.get(0).getFormattedMessage())
                .contains("203.0.113.7")
                .doesNotContain("8.8.8.8");
    }

    @Test
    @DisplayName("client address falls back to the remote address without CF-Connecting-IP")
    void clientAddressFallsBackToRemoteAddress() throws Exception {
        mockMvc.perform(get("/api/.env")
                        .header("X-Forwarded-For", "8.8.8.8")
                        .with(request -> {
                            request.setRemoteAddr("198.51.100.4");
                            return request;
                        }))
                .andExpect(status().isNotFound());

        assertThat(logEvents.list).hasSize(1);
        assertThat(logEvents.list.get(0).getFormattedMessage())
                .contains("198.51.100.4")
                .doesNotContain("8.8.8.8");
    }

    static Stream<Arguments> clientErrors() {
        return Stream.of(
                Arguments.of(new MethodArgumentTypeMismatchException("foo", Category.class, "category", null, null),
                        HttpStatus.BAD_REQUEST),
                Arguments.of(new PropertyReferenceException("password", TypeInformation.of(String.class), List.of()),
                        HttpStatus.BAD_REQUEST),
                Arguments.of(new HttpMessageNotReadableException("bad json", new MockHttpInputMessage(new byte[0])),
                        HttpStatus.BAD_REQUEST),
                Arguments.of(new MissingServletRequestParameterException("page", "int"), HttpStatus.BAD_REQUEST),
                Arguments.of(new MissingServletRequestPartException("file"), HttpStatus.BAD_REQUEST),
                Arguments.of(new MultipartException("Current request is not a multipart request"),
                        HttpStatus.BAD_REQUEST),
                Arguments.of(new HttpRequestMethodNotSupportedException("PUT"), HttpStatus.METHOD_NOT_ALLOWED),
                Arguments.of(new HttpMediaTypeNotSupportedException("text/plain not supported"),
                        HttpStatus.UNSUPPORTED_MEDIA_TYPE),
                Arguments.of(new MaxUploadSizeExceededException(1024), HttpStatus.CONTENT_TOO_LARGE),
                Arguments.of(new DataIntegrityViolationException("duplicate key value violates unique constraint"),
                        HttpStatus.CONFLICT),
                Arguments.of(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad thing"), HttpStatus.BAD_REQUEST)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("clientErrors")
    @DisplayName("client-side errors keep their status, answer with ApiResponse and log one WARN without a stack trace")
    void clientErrorsAreMappedWithOneWarn(Exception thrown, HttpStatus expected) throws Exception {
        // willAnswer: several of these are checked exceptions the service signature does not declare
        given(postService.getAllPublishedPosts("en")).willAnswer(invocation -> {
            throw thrown;
        });

        mockMvc.perform(get("/api/posts").header("CF-Connecting-IP", "203.0.113.7"))
                .andExpect(status().is(expected.value()))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty());

        assertThat(logEvents.list).hasSize(1);
        ILoggingEvent event = logEvents.list.get(0);
        assertThat(event.getLevel()).isEqualTo(Level.WARN);
        assertThat(event.getThrowableProxy()).isNull();
        assertThat(event.getFormattedMessage()).contains("GET").contains("/api/posts").contains("203.0.113.7");
    }

    @Test
    @DisplayName("other framework 4xx errors (ErrorResponse) are answered by their own status with one WARN")
    void otherFrameworkClientErrorsKeepTheirStatus() throws Exception {
        // Spring reads the parameter in getMessage(), so the exception needs a real one, as MVC would supply
        MethodParameter header = new MethodParameter(
                PostController.class.getMethod("getPost", String.class, String.class), 1);
        given(postService.getAllPublishedPosts("en")).willAnswer(invocation -> {
            throw new MissingRequestHeaderException("X-Required", header);
        });

        mockMvc.perform(get("/api/posts").header("CF-Connecting-IP", "203.0.113.7"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        assertThat(logEvents.list).hasSize(1);
        assertThat(logEvents.list.get(0).getLevel()).isEqualTo(Level.WARN);
        assertThat(logEvents.list.get(0).getThrowableProxy()).isNull();
    }

    @Test
    @DisplayName("GET /api/posts with Accept: text/html returns 406 without a body and logs one WARN, no ERROR")
    void unacceptableMediaTypeReturns406WithOneWarn() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .header("Accept", "text/html")
                        .header("CF-Connecting-IP", "203.0.113.7"))
                .andExpect(status().isNotAcceptable())
                .andExpect(content().string(""));

        assertThat(logEvents.list).hasSize(1);
        ILoggingEvent event = logEvents.list.get(0);
        assertThat(event.getLevel()).isEqualTo(Level.WARN);
        assertThat(event.getThrowableProxy()).isNull();
        assertThat(event.getFormattedMessage()).contains("406").contains("/api/posts").contains("203.0.113.7");
    }

    @Test
    @DisplayName("HttpMediaTypeNotAcceptableException thrown by a handler also answers 406 with one WARN")
    void thrownNotAcceptableReturns406() throws Exception {
        given(postService.getAllPublishedPosts("en")).willAnswer(invocation -> {
            throw new HttpMediaTypeNotAcceptableException("no acceptable representation");
        });

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isNotAcceptable());

        assertThat(logEvents.list).extracting(ILoggingEvent::getLevel).containsExactly(Level.WARN);
    }

    @Test
    @DisplayName("GET /api/posts?category=foo returns 400 and one WARN")
    void unknownCategoryParamReturns400() throws Exception {
        mockMvc.perform(get("/api/posts").param("category", "foo"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        assertThat(logEvents.list).extracting(ILoggingEvent::getLevel).containsExactly(Level.WARN);
    }

    @Test
    @DisplayName("POST /api/posts returns 405 and one WARN")
    void wrongMethodReturns405() throws Exception {
        mockMvc.perform(post("/api/posts"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.success").value(false));

        assertThat(logEvents.list).extracting(ILoggingEvent::getLevel).containsExactly(Level.WARN);
    }

    @Test
    @DisplayName("ResponseStatusException 503 answers with its reason and logs exactly one ERROR including the cause")
    void serviceUnavailableLogsOneErrorWithCause() throws Exception {
        RuntimeException cause = new RuntimeException("smtp down");
        given(postService.getAllPublishedPosts("en"))
                .willThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Message could not be sent", cause));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Message could not be sent"));

        assertThat(logEvents.list).hasSize(1);
        ILoggingEvent event = logEvents.list.get(0);
        assertThat(event.getLevel()).isEqualTo(Level.ERROR);
        assertThat(event.getThrowableProxy()).isNotNull();
        assertThat(event.getThrowableProxy().getCause().getMessage()).isEqualTo("smtp down");
    }

    @Test
    @DisplayName("data integrity violation answers 409 without leaking SQL and names only the cause class in the log")
    void dataIntegrityViolationLogsCauseClassOnly() throws Exception {
        given(postService.getAllPublishedPosts("en")).willThrow(new DataIntegrityViolationException(
                "could not execute statement", new IllegalStateException("Key (slug)=(secret-value) already exists")));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "The data conflicts with an existing record or exceeds a field limit"));

        assertThat(logEvents.list).hasSize(1);
        assertThat(logEvents.list.get(0).getFormattedMessage())
                .contains("IllegalStateException")
                .doesNotContain("secret-value");
    }

    @Test
    @DisplayName("unmapped exceptions still answer 500 with one ERROR")
    void unexpectedExceptionStaysInternalServerError() throws Exception {
        given(postService.getAllPublishedPosts("en")).willThrow(new IllegalStateException("boom"));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isInternalServerError());

        assertThat(logEvents.list).extracting(ILoggingEvent::getLevel).containsExactly(Level.ERROR);
    }
}
