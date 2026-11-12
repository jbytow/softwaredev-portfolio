package com.portfolio.controller;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.portfolio.config.GlobalExceptionHandler;
import com.portfolio.config.TestSecurityConfig;
import com.portfolio.security.JwtService;
import com.portfolio.service.StorageService;
import com.portfolio.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = MediaController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class
        }
)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("MediaController")
class MediaControllerTest {

    private static final String IMAGE_NAME = "3f2b6c1e-8a4d-4f7e-9b1a-2c3d4e5f6a7b.jpg";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StorageService storageService;

    @MockBean
    private JwtService jwtService;

    @MockBean
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

    @Nested
    @DisplayName("GET /api/media/{subDir}/{filename}")
    class ServeMedia {

        @Test
        @DisplayName("existing file is served with its Content-Type")
        void servesExistingFile() throws Exception {
            given(storageService.loadAsResource("images/" + IMAGE_NAME))
                    .willReturn(new ByteArrayResource(new byte[]{1, 2, 3}));

            mockMvc.perform(get("/api/media/images/" + IMAGE_NAME))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", "image/jpeg"))
                    .andExpect(content().bytes(new byte[]{1, 2, 3}));
        }

        @Test
        @DisplayName("legacy .jfif file is served as image/jpeg")
        void servesLegacyJfifAsJpeg() throws Exception {
            String legacyName = "3f2b6c1e-8a4d-4f7e-9b1a-2c3d4e5f6a7b.jfif";
            given(storageService.loadAsResource("images/" + legacyName))
                    .willReturn(new ByteArrayResource(new byte[]{1}));

            mockMvc.perform(get("/api/media/images/" + legacyName))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", "image/jpeg"));
        }

        @Test
        @DisplayName("UUID-named file is cacheable for a year after the security headers are applied")
        void servesFileAsImmutable() throws Exception {
            given(storageService.loadAsResource("images/" + IMAGE_NAME))
                    .willReturn(new ByteArrayResource(new byte[]{1, 2, 3}));

            mockMvc.perform(get("/api/media/images/" + IMAGE_NAME))
                    .andExpect(status().isOk())
                    .andExpect(header().stringValues(HttpHeaders.CACHE_CONTROL, "max-age=31536000, public, immutable"))
                    .andExpect(header().doesNotExist(HttpHeaders.PRAGMA))
                    .andExpect(header().doesNotExist(HttpHeaders.EXPIRES));
        }

        @Test
        @DisplayName("missing file -> 404 JSON and no ERROR log")
        void missingFileIsNotFound() throws Exception {
            given(storageService.loadAsResource("images/missing.jpg"))
                    .willThrow(new EntityNotFoundException("Media file not found: images/missing.jpg"));

            mockMvc.perform(get("/api/media/images/missing.jpg"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));

            assertNoErrorLogged();
        }

        @ParameterizedTest
        @ValueSource(strings = {"/api/media/wp-includes/wlwmanifest.xml", "/api/media/other/x.jpg", "/api/media/IMAGES/x.jpg"})
        @DisplayName("unknown subdirectory -> 404 without touching storage")
        void unknownSubdirectoryIsNotFound(String path) throws Exception {
            mockMvc.perform(get(path))
                    .andExpect(status().isNotFound());

            verifyNoInteractions(storageService);
            assertNoErrorLogged();
        }
    }

    @Nested
    @DisplayName("GET /api/media/{single segment}")
    class SingleSegment {

        @ParameterizedTest
        @ValueSource(strings = {"/api/media/3f2b6c1e-8a4d-4f7e-9b1a-2c3d4e5f6a7b", "/api/media/.env", "/api/media/foo.php"})
        @DisplayName("no public metadata endpoint: nothing is served")
        void singleSegmentServesNothing(String path) throws Exception {
            // The 404 (instead of 500) for an unmapped path comes from GlobalExceptionHandler, owned by
            // another package; here only the removal of the endpoint is asserted.
            mockMvc.perform(get(path))
                    .andExpect(status().is(org.hamcrest.Matchers.not(200)))
                    .andExpect(jsonPath("$.data.id").doesNotExist());

            verifyNoInteractions(storageService);
        }
    }

    private void assertNoErrorLogged() {
        assertThat(logEvents.list).noneMatch(event -> event.getLevel() == Level.ERROR);
    }
}
