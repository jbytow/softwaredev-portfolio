package com.portfolio.controller;

import com.portfolio.config.TestSecurityConfig;
import com.portfolio.repository.MediaRepository;
import com.portfolio.repository.PostRepository;
import com.portfolio.security.JwtService;
import com.portfolio.service.MediaService;
import com.portfolio.service.StorageService;
import com.portfolio.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminMediaController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration.class
        }
)
@Import({TestSecurityConfig.class, MediaService.class})
@ActiveProfiles("test")
@DisplayName("AdminMediaController")
@WithMockUser(roles = "ADMIN")
class AdminMediaControllerTest {

    private static final String TOO_LONG_ALT = "a".repeat(256);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MediaRepository mediaRepository;

    @MockitoBean
    private PostRepository postRepository;

    @MockitoBean
    private StorageService storageService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("POST /api/admin/media returns 400 for an alt text over 255 characters without storing the file")
    void uploadRejectsTooLongAltText() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/admin/media").file(file).param("altTextEn", TOO_LONG_ALT))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(storageService);
    }

    @Test
    @DisplayName("POST /api/admin/media/youtube returns 400 for a video URL over 500 characters")
    void youtubeRejectsTooLongUrl() throws Exception {
        String url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ&x=" + "a".repeat(500);

        mockMvc.perform(post("/api/admin/media/youtube").param("videoUrl", url))
                .andExpect(status().isBadRequest());

        verify(mediaRepository, never()).save(any());
    }

    @Test
    @DisplayName("PUT /api/admin/media/{id} returns 400 for an alt text over 255 characters without saving")
    void updateRejectsTooLongAltText() throws Exception {
        mockMvc.perform(put("/api/admin/media/{id}", UUID.randomUUID()).param("altTextPl", TOO_LONG_ALT))
                .andExpect(status().isBadRequest());

        verify(mediaRepository, never()).save(any());
    }

    @Test
    @DisplayName("PATCH /api/admin/media/reorder returns 400 for an empty item list without reordering")
    void reorderRejectsEmptyList() throws Exception {
        mockMvc.perform(patch("/api/admin/media/reorder").param("postId", UUID.randomUUID().toString())
                        .contentType("application/json").content("{\"items\":[]}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(mediaRepository);
    }
}
