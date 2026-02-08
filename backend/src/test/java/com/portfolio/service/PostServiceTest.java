package com.portfolio.service;

import com.portfolio.dto.PostCreateRequest;
import com.portfolio.dto.PostDto;
import com.portfolio.dto.PostUpdateRequest;
import com.portfolio.entity.Category;
import com.portfolio.entity.Post;
import com.portfolio.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService")
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Captor
    private ArgumentCaptor<Post> postCaptor;

    private Post samplePost;
    private UUID postId;

    @BeforeEach
    void setUp() {
        postId = UUID.randomUUID();
        samplePost = Post.builder()
                .id(postId)
                .category(Category.PERSONAL_PROJECT)
                .titleEn("Test Title EN")
                .titlePl("Test Title PL")
                .slug("test-title-en")
                .excerptEn("Excerpt EN")
                .excerptPl("Excerpt PL")
                .published(false)
                .displayOrder(1)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("getAllPublishedPosts")
    class GetAllPublishedPosts {

        @Test
        @DisplayName("should return all published posts")
        void shouldReturnAllPublishedPosts() {
            Post publishedPost = Post.builder()
                    .id(UUID.randomUUID())
                    .category(Category.PERSONAL_PROJECT)
                    .titleEn("Published Post")
                    .titlePl("Opublikowany Post")
                    .slug("published-post")
                    .published(true)
                    .build();
            given(postRepository.findAllPublished()).willReturn(List.of(publishedPost));

            List<PostDto> result = postService.getAllPublishedPosts("en");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Published Post");
        }

        @Test
        @DisplayName("should return empty list when no published posts")
        void shouldReturnEmptyListWhenNoPublishedPosts() {
            given(postRepository.findAllPublished()).willReturn(List.of());

            List<PostDto> result = postService.getAllPublishedPosts("en");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should use Polish title when locale is pl")
        void shouldUsePolishTitleWhenLocaleIsPl() {
            Post post = Post.builder()
                    .id(UUID.randomUUID())
                    .category(Category.PERSONAL_PROJECT)
                    .titleEn("English Title")
                    .titlePl("Polski Tytuł")
                    .slug("post")
                    .published(true)
                    .build();
            given(postRepository.findAllPublished()).willReturn(List.of(post));

            List<PostDto> result = postService.getAllPublishedPosts("pl");

            assertThat(result.get(0).getTitle()).isEqualTo("Polski Tytuł");
        }
    }

    @Nested
    @DisplayName("getPostsByCategory")
    class GetPostsByCategory {

        @Test
        @DisplayName("should return posts for given category")
        void shouldReturnPostsForCategory() {
            given(postRepository.findByCategoryAndPublishedTrueOrderByDisplayOrderAsc(Category.PERSONAL_PROJECT))
                    .willReturn(List.of(samplePost));

            List<PostDto> result = postService.getPostsByCategory(Category.PERSONAL_PROJECT, "en");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCategory()).isEqualTo(Category.PERSONAL_PROJECT);
        }

        @Test
        @DisplayName("should return empty list when no posts in category")
        void shouldReturnEmptyListWhenNoPosts() {
            given(postRepository.findByCategoryAndPublishedTrueOrderByDisplayOrderAsc(Category.PERSONAL_PROJECT))
                    .willReturn(List.of());

            List<PostDto> result = postService.getPostsByCategory(Category.PERSONAL_PROJECT, "en");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getPostBySlug")
    class GetPostBySlug {

        @Test
        @DisplayName("should return post when found by slug")
        void shouldReturnPostWhenFound() {
            given(postRepository.findBySlug("test-slug")).willReturn(Optional.of(samplePost));

            PostDto result = postService.getPostBySlug("test-slug", "en");

            assertThat(result).isNotNull();
            assertThat(result.getSlug()).isEqualTo("test-title-en");
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when post not found")
        void shouldThrowExceptionWhenNotFound() {
            given(postRepository.findBySlug("non-existent")).willReturn(Optional.empty());

            assertThatThrownBy(() -> postService.getPostBySlug("non-existent", "en"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("non-existent");
        }
    }

    @Nested
    @DisplayName("getPostById")
    class GetPostById {

        @Test
        @DisplayName("should return post when found by id")
        void shouldReturnPostWhenFound() {
            given(postRepository.findById(postId)).willReturn(Optional.of(samplePost));

            PostDto result = postService.getPostById(postId, "en");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(postId);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when post not found")
        void shouldThrowExceptionWhenNotFound() {
            UUID nonExistentId = UUID.randomUUID();
            given(postRepository.findById(nonExistentId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> postService.getPostById(nonExistentId, "en"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("createPost")
    class CreatePost {

        @Test
        @DisplayName("should create post with provided data")
        void shouldCreatePostWithProvidedData() {
            PostCreateRequest request = new PostCreateRequest();
            request.setCategory(Category.PERSONAL_PROJECT);
            request.setTitleEn("New Post");
            request.setTitlePl("Nowy Post");
            request.setPublished(false);

            given(postRepository.existsBySlug(anyString())).willReturn(false);
            given(postRepository.getMaxDisplayOrder(Category.PERSONAL_PROJECT)).willReturn(0);
            given(postRepository.save(any(Post.class))).willAnswer(invocation -> {
                Post post = invocation.getArgument(0);
                post.setId(UUID.randomUUID());
                return post;
            });

            PostDto result = postService.createPost(request, "en");

            verify(postRepository).save(postCaptor.capture());
            Post savedPost = postCaptor.getValue();

            assertThat(savedPost.getTitleEn()).isEqualTo("New Post");
            assertThat(savedPost.getTitlePl()).isEqualTo("Nowy Post");
            assertThat(savedPost.getCategory()).isEqualTo(Category.PERSONAL_PROJECT);
        }

        @Test
        @DisplayName("should generate unique slug when not provided")
        void shouldGenerateUniqueSlug() {
            PostCreateRequest request = new PostCreateRequest();
            request.setCategory(Category.PERSONAL_PROJECT);
            request.setTitleEn("My Great Post");
            request.setTitlePl("Mój Świetny Post");

            given(postRepository.existsBySlug("my-great-post")).willReturn(false);
            given(postRepository.getMaxDisplayOrder(any())).willReturn(0);
            given(postRepository.save(any(Post.class))).willAnswer(invocation -> {
                Post post = invocation.getArgument(0);
                post.setId(UUID.randomUUID());
                return post;
            });

            postService.createPost(request, "en");

            verify(postRepository).save(postCaptor.capture());
            assertThat(postCaptor.getValue().getSlug()).isEqualTo("my-great-post");
        }

        @Test
        @DisplayName("should append number to slug when already exists")
        void shouldAppendNumberToSlugWhenExists() {
            PostCreateRequest request = new PostCreateRequest();
            request.setCategory(Category.PERSONAL_PROJECT);
            request.setTitleEn("Duplicate Title");
            request.setTitlePl("Duplikat");

            given(postRepository.existsBySlug("duplicate-title")).willReturn(true);
            given(postRepository.existsBySlug("duplicate-title-1")).willReturn(false);
            given(postRepository.getMaxDisplayOrder(any())).willReturn(0);
            given(postRepository.save(any(Post.class))).willAnswer(invocation -> {
                Post post = invocation.getArgument(0);
                post.setId(UUID.randomUUID());
                return post;
            });

            postService.createPost(request, "en");

            verify(postRepository).save(postCaptor.capture());
            assertThat(postCaptor.getValue().getSlug()).isEqualTo("duplicate-title-1");
        }

        @Test
        @DisplayName("should set default published to false when not provided")
        void shouldSetDefaultPublishedFalse() {
            PostCreateRequest request = new PostCreateRequest();
            request.setCategory(Category.PERSONAL_PROJECT);
            request.setTitleEn("Test");
            request.setTitlePl("Test");
            // published is not set

            given(postRepository.existsBySlug(anyString())).willReturn(false);
            given(postRepository.getMaxDisplayOrder(any())).willReturn(0);
            given(postRepository.save(any(Post.class))).willAnswer(invocation -> {
                Post post = invocation.getArgument(0);
                post.setId(UUID.randomUUID());
                return post;
            });

            postService.createPost(request, "en");

            verify(postRepository).save(postCaptor.capture());
            assertThat(postCaptor.getValue().getPublished()).isFalse();
        }
    }

    @Nested
    @DisplayName("updatePost")
    class UpdatePost {

        @Test
        @DisplayName("should update post fields when provided")
        void shouldUpdatePostFields() {
            given(postRepository.findById(postId)).willReturn(Optional.of(samplePost));
            given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

            PostUpdateRequest request = new PostUpdateRequest();
            request.setTitleEn("Updated Title");

            PostDto result = postService.updatePost(postId, request, "en");

            verify(postRepository).save(postCaptor.capture());
            assertThat(postCaptor.getValue().getTitleEn()).isEqualTo("Updated Title");
        }

        @Test
        @DisplayName("should not update fields that are null in request")
        void shouldNotUpdateNullFields() {
            String originalTitle = samplePost.getTitleEn();
            given(postRepository.findById(postId)).willReturn(Optional.of(samplePost));
            given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

            PostUpdateRequest request = new PostUpdateRequest();
            request.setExcerptEn("New Excerpt");
            // titleEn is null

            postService.updatePost(postId, request, "en");

            verify(postRepository).save(postCaptor.capture());
            assertThat(postCaptor.getValue().getTitleEn()).isEqualTo(originalTitle);
            assertThat(postCaptor.getValue().getExcerptEn()).isEqualTo("New Excerpt");
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when post not found")
        void shouldThrowExceptionWhenNotFound() {
            UUID nonExistentId = UUID.randomUUID();
            given(postRepository.findById(nonExistentId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> postService.updatePost(nonExistentId, new PostUpdateRequest(), "en"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deletePost")
    class DeletePost {

        @Test
        @DisplayName("should delete post when exists")
        void shouldDeletePostWhenExists() {
            given(postRepository.existsById(postId)).willReturn(true);

            postService.deletePost(postId);

            verify(postRepository).deleteById(postId);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when post not found")
        void shouldThrowExceptionWhenNotFound() {
            UUID nonExistentId = UUID.randomUUID();
            given(postRepository.existsById(nonExistentId)).willReturn(false);

            assertThatThrownBy(() -> postService.deletePost(nonExistentId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("togglePublish")
    class TogglePublish {

        @Test
        @DisplayName("should toggle published from false to true")
        void shouldToggleFromFalseToTrue() {
            samplePost.setPublished(false);
            given(postRepository.findById(postId)).willReturn(Optional.of(samplePost));
            given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

            PostDto result = postService.togglePublish(postId, "en");

            verify(postRepository).save(postCaptor.capture());
            assertThat(postCaptor.getValue().getPublished()).isTrue();
        }

        @Test
        @DisplayName("should toggle published from true to false")
        void shouldToggleFromTrueToFalse() {
            samplePost.setPublished(true);
            given(postRepository.findById(postId)).willReturn(Optional.of(samplePost));
            given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

            PostDto result = postService.togglePublish(postId, "en");

            verify(postRepository).save(postCaptor.capture());
            assertThat(postCaptor.getValue().getPublished()).isFalse();
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when post not found")
        void shouldThrowExceptionWhenNotFound() {
            UUID nonExistentId = UUID.randomUUID();
            given(postRepository.findById(nonExistentId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> postService.togglePublish(nonExistentId, "en"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAllPosts (admin)")
    class GetAllPosts {

        @Test
        @DisplayName("should return all posts including unpublished")
        void shouldReturnAllPostsIncludingUnpublished() {
            Post unpublished = Post.builder()
                    .id(UUID.randomUUID())
                    .category(Category.PERSONAL_PROJECT)
                    .titleEn("Unpublished")
                    .titlePl("Nieopublikowany")
                    .slug("unpublished")
                    .published(false)
                    .build();
            Post published = Post.builder()
                    .id(UUID.randomUUID())
                    .category(Category.PERSONAL_PROJECT)
                    .titleEn("Published")
                    .titlePl("Opublikowany")
                    .slug("published")
                    .published(true)
                    .build();

            given(postRepository.findAll()).willReturn(List.of(unpublished, published));

            List<PostDto> result = postService.getAllPosts("en");

            assertThat(result).hasSize(2);
        }
    }
}
