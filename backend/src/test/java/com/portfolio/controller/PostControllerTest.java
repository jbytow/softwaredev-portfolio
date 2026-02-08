package com.portfolio.controller;

import com.portfolio.config.TestSecurityConfig;
import com.portfolio.dto.PostDto;
import com.portfolio.entity.Category;
import com.portfolio.security.JwtService;
import com.portfolio.service.PostService;
import com.portfolio.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = PostController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class
        }
)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("PostController")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserService userService;

    private PostDto createSamplePostDto() {
        return PostDto.builder()
                .id(UUID.randomUUID())
                .category(Category.PERSONAL_PROJECT)
                .categoryLabel("Campaigns")
                .title("Sample Post")
                .titleEn("Sample Post")
                .titlePl("Przykładowy Post")
                .slug("sample-post")
                .excerpt("Sample excerpt")
                .published(true)
                .displayOrder(1)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("GET /api/posts")
    class GetPosts {

        @Test
        @DisplayName("should return all published posts")
        void shouldReturnAllPublishedPosts() throws Exception {
            List<PostDto> posts = List.of(createSamplePostDto());
            given(postService.getAllPublishedPosts("en")).willReturn(posts);

            mockMvc.perform(get("/api/posts")
                            .header("Accept-Language", "en")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].title").value("Sample Post"));
        }

        @Test
        @DisplayName("should return empty array when no posts")
        void shouldReturnEmptyArrayWhenNoPosts() throws Exception {
            given(postService.getAllPublishedPosts("en")).willReturn(Collections.emptyList());

            mockMvc.perform(get("/api/posts")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("should filter by category when provided")
        void shouldFilterByCategory() throws Exception {
            PostDto experiencePost = createSamplePostDto();
            given(postService.getPostsByCategory(eq(Category.PERSONAL_PROJECT), eq("en")))
                    .willReturn(List.of(experiencePost));

            mockMvc.perform(get("/api/posts")
                            .param("category", "PERSONAL_PROJECT")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].category").value("PERSONAL_PROJECT"));
        }

        @Test
        @DisplayName("should use Polish locale when Accept-Language is pl")
        void shouldUsePolishLocale() throws Exception {
            PostDto post = PostDto.builder()
                    .id(UUID.randomUUID())
                    .category(Category.PERSONAL_PROJECT)
                    .categoryLabel("Doświadczenie")
                    .title("Polski Tytuł")
                    .titleEn("English Title")
                    .titlePl("Polski Tytuł")
                    .slug("post")
                    .published(true)
                    .build();
            given(postService.getAllPublishedPosts("pl")).willReturn(List.of(post));

            mockMvc.perform(get("/api/posts")
                            .header("Accept-Language", "pl")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].title").value("Polski Tytuł"));
        }

        @Test
        @DisplayName("should default to English when no Accept-Language header")
        void shouldDefaultToEnglish() throws Exception {
            given(postService.getAllPublishedPosts("en")).willReturn(Collections.emptyList());

            mockMvc.perform(get("/api/posts")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Verify that service was called with "en" locale
            org.mockito.Mockito.verify(postService).getAllPublishedPosts("en");
        }
    }

    @Nested
    @DisplayName("GET /api/posts/{slug}")
    class GetPostBySlug {

        @Test
        @DisplayName("should return post by slug")
        void shouldReturnPostBySlug() throws Exception {
            PostDto post = createSamplePostDto();
            given(postService.getPostBySlug("sample-post", "en")).willReturn(post);

            mockMvc.perform(get("/api/posts/sample-post")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.slug").value("sample-post"));
        }

        @Test
        @DisplayName("should return 404 when post not found")
        void shouldReturn404WhenNotFound() throws Exception {
            given(postService.getPostBySlug("non-existent", "en"))
                    .willThrow(new jakarta.persistence.EntityNotFoundException("Post not found"));

            mockMvc.perform(get("/api/posts/non-existent")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Language parsing")
    class LanguageParsing {

        @Test
        @DisplayName("should parse complex Accept-Language header")
        void shouldParseComplexAcceptLanguageHeader() throws Exception {
            given(postService.getAllPublishedPosts("pl")).willReturn(Collections.emptyList());

            mockMvc.perform(get("/api/posts")
                            .header("Accept-Language", "pl-PL,pl;q=0.9,en-US;q=0.8,en;q=0.7")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            org.mockito.Mockito.verify(postService).getAllPublishedPosts("pl");
        }

        @Test
        @DisplayName("should fallback to English for unsupported languages")
        void shouldFallbackToEnglishForUnsupportedLanguages() throws Exception {
            given(postService.getAllPublishedPosts("en")).willReturn(Collections.emptyList());

            mockMvc.perform(get("/api/posts")
                            .header("Accept-Language", "de-DE")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            org.mockito.Mockito.verify(postService).getAllPublishedPosts("en");
        }
    }
}
