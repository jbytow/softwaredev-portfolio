package com.portfolio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.config.TestSecurityConfig;
import com.portfolio.dto.PostCreateRequest;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AdminPostController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class
        }
)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("AdminPostController")
class AdminPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
                .published(false)
                .displayOrder(1)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("GET /api/admin/posts")
    class GetAllPosts {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return all posts for authenticated admin")
        void shouldReturnAllPostsForAdmin() throws Exception {
            List<PostDto> posts = List.of(createSamplePostDto());
            given(postService.getAllPosts("en")).willReturn(posts);

            mockMvc.perform(get("/api/admin/posts")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", hasSize(1)));
        }

        @Test
        @DisplayName("should return 403 for unauthenticated user")
        void shouldReturn403ForUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/admin/posts")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should filter by category when provided")
        void shouldFilterByCategory() throws Exception {
            PostDto post = createSamplePostDto();
            given(postService.getAllPostsByCategory(Category.PERSONAL_PROJECT, "en"))
                    .willReturn(List.of(post));

            mockMvc.perform(get("/api/admin/posts")
                            .param("category", "PERSONAL_PROJECT")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].category").value("PERSONAL_PROJECT"));
        }
    }

    @Nested
    @DisplayName("GET /api/admin/posts/{id}")
    class GetPostById {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return post by id for authenticated admin")
        void shouldReturnPostById() throws Exception {
            UUID postId = UUID.randomUUID();
            PostDto post = createSamplePostDto();
            given(postService.getPostById(postId, "en")).willReturn(post);

            mockMvc.perform(get("/api/admin/posts/" + postId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("Sample Post"));
        }
    }

    @Nested
    @DisplayName("POST /api/admin/posts")
    class CreatePost {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should create post for authenticated admin")
        void shouldCreatePost() throws Exception {
            PostCreateRequest request = new PostCreateRequest();
            request.setCategory(Category.PERSONAL_PROJECT);
            request.setTitleEn("New Post");
            request.setTitlePl("Nowy Post");

            PostDto createdPost = createSamplePostDto();
            given(postService.createPost(any(PostCreateRequest.class), eq("en")))
                    .willReturn(createdPost);

            mockMvc.perform(post("/api/admin/posts")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("should return 403 for unauthenticated user")
        void shouldReturn403ForUnauthenticated() throws Exception {
            PostCreateRequest request = new PostCreateRequest();
            request.setCategory(Category.PERSONAL_PROJECT);
            request.setTitleEn("New Post");
            request.setTitlePl("Nowy Post");

            mockMvc.perform(post("/api/admin/posts")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/posts/{id}")
    class DeletePost {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should delete post for authenticated admin")
        void shouldDeletePost() throws Exception {
            UUID postId = UUID.randomUUID();

            mockMvc.perform(delete("/api/admin/posts/" + postId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(postService).deletePost(postId);
        }
    }

    @Nested
    @DisplayName("PATCH /api/admin/posts/{id}/publish")
    class TogglePublish {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should toggle publish status for authenticated admin")
        void shouldTogglePublish() throws Exception {
            UUID postId = UUID.randomUUID();
            PostDto updatedPost = PostDto.builder()
                    .id(postId)
                    .published(true)
                    .build();
            given(postService.togglePublish(postId, "en")).willReturn(updatedPost);

            mockMvc.perform(patch("/api/admin/posts/" + postId + "/publish")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
