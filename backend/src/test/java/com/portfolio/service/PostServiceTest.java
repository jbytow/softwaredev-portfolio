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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
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

    @Mock
    private MediaService mediaService;

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
            samplePost.setPublished(true);
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
    @DisplayName("getPostBySlug (drafts)")
    class GetPostBySlugDrafts {

        @ParameterizedTest
        @ValueSource(strings = {"en", "pl"})
        @DisplayName("should hide a draft behind the same not-found answer as a missing slug")
        void shouldHideDraft(String locale) {
            given(postRepository.findBySlug("test-title-en")).willReturn(Optional.of(samplePost));

            assertThatThrownBy(() -> postService.getPostBySlug("test-title-en", locale))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Post not found: test-title-en");
        }
    }

    @Nested
    @DisplayName("hasCaseStudy")
    class HasCaseStudy {

        @BeforeEach
        void publish() {
            samplePost.setPublished(true);
        }

        @ParameterizedTest
        @CsvSource({"en,true", "pl,false"})
        @DisplayName("should report an English-only case study only for the en locale")
        void englishOnlyCaseStudy(String locale, boolean expected) {
            samplePost.setCaseStudyChallengeEn("Challenge EN");
            given(postRepository.findBySlug("test-title-en")).willReturn(Optional.of(samplePost));

            assertThat(postService.getPostBySlug("test-title-en", locale).getHasCaseStudy()).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({"en,false", "pl,true"})
        @DisplayName("should report a Polish-only case study only for the pl locale")
        void polishOnlyCaseStudy(String locale, boolean expected) {
            samplePost.setCaseStudyResultsPl("Wyniki PL");
            given(postRepository.findBySlug("test-title-en")).willReturn(Optional.of(samplePost));

            assertThat(postService.getPostBySlug("test-title-en", locale).getHasCaseStudy()).isEqualTo(expected);
        }

        @Test
        @DisplayName("should count a testimonial-only case study")
        void testimonialOnlyCaseStudy() {
            samplePost.setCaseStudyTestimonialPl("Polecam");
            given(postRepository.findBySlug("test-title-en")).willReturn(Optional.of(samplePost));

            assertThat(postService.getPostBySlug("test-title-en", "pl").getHasCaseStudy()).isTrue();
        }

        @Test
        @DisplayName("should ignore blank case-study fields")
        void blankCaseStudy() {
            samplePost.setCaseStudyChallengeEn("   ");
            samplePost.setCaseStudySolutionEn("");
            given(postRepository.findBySlug("test-title-en")).willReturn(Optional.of(samplePost));

            assertThat(postService.getPostBySlug("test-title-en", "en").getHasCaseStudy()).isFalse();
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
        @DisplayName("should delete the post's media before the post itself")
        void shouldDeleteMediaBeforePost() {
            given(postRepository.existsById(postId)).willReturn(true);

            postService.deletePost(postId);

            InOrder order = inOrder(mediaService, postRepository);
            order.verify(mediaService).deleteAllForPost(postId);
            order.verify(postRepository).deleteById(postId);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when post not found")
        void shouldThrowExceptionWhenNotFound() {
            UUID nonExistentId = UUID.randomUUID();
            given(postRepository.existsById(nonExistentId)).willReturn(false);

            assertThatThrownBy(() -> postService.deletePost(nonExistentId))
                    .isInstanceOf(EntityNotFoundException.class);
            verifyNoInteractions(mediaService);
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

    @Nested
    @DisplayName("createPost (slug)")
    class CreatePostSlug {

        @Test
        @DisplayName("should normalize a provided slug")
        void shouldNormalizeProvidedSlug() {
            PostCreateRequest request = newCreateRequest("Some Title");
            request.setSlug(" My Slug ");
            stubSaveForCreate();

            postService.createPost(request, "en");

            verify(postRepository).save(postCaptor.capture());
            assertThat(postCaptor.getValue().getSlug()).isEqualTo("my-slug");
        }

        @Test
        @DisplayName("should append a number to a provided slug that is already taken")
        void shouldMakeProvidedSlugUnique() {
            PostCreateRequest request = newCreateRequest("Some Title");
            request.setSlug("taken-slug");
            stubSaveForCreate();
            given(postRepository.existsBySlug("taken-slug")).willReturn(true);

            postService.createPost(request, "en");

            verify(postRepository).save(postCaptor.capture());
            assertThat(postCaptor.getValue().getSlug()).isEqualTo("taken-slug-1");
        }

        @Test
        @DisplayName("should generate the slug from the title when the provided slug is blank")
        void shouldGenerateSlugWhenProvidedSlugIsBlank() {
            PostCreateRequest request = newCreateRequest("Kampania Łódź");
            request.setSlug("   ");
            stubSaveForCreate();

            postService.createPost(request, "en");

            verify(postRepository).save(postCaptor.capture());
            assertThat(postCaptor.getValue().getSlug()).isEqualTo("kampania-lodz");
        }

        @Test
        @DisplayName("should never store a blank slug for a symbol-only title")
        void shouldNotStoreBlankSlugForSymbolTitle() {
            PostCreateRequest request = newCreateRequest("!!! ???");
            stubSaveForCreate();

            postService.createPost(request, "en");

            verify(postRepository).save(postCaptor.capture());
            assertThat(postCaptor.getValue().getSlug()).isNotBlank().doesNotStartWith("-");
        }

        private PostCreateRequest newCreateRequest(String titleEn) {
            PostCreateRequest request = new PostCreateRequest();
            request.setCategory(Category.PERSONAL_PROJECT);
            request.setTitleEn(titleEn);
            request.setTitlePl(titleEn);
            return request;
        }

        private void stubSaveForCreate() {
            lenient().when(postRepository.existsBySlug(anyString())).thenReturn(false);
            given(postRepository.getMaxDisplayOrder(any())).willReturn(0);
            given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));
        }
    }

    @Nested
    @DisplayName("updatePost (slug)")
    class UpdatePostSlug {

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("should keep the existing slug when the slug field is cleared")
        void shouldKeepSlugWhenBlank(String blankSlug) {
            given(postRepository.findById(postId)).willReturn(Optional.of(samplePost));
            given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));
            PostUpdateRequest request = new PostUpdateRequest();
            request.setSlug(blankSlug);

            postService.updatePost(postId, request, "en");

            verify(postRepository).save(postCaptor.capture());
            assertThat(postCaptor.getValue().getSlug()).isEqualTo("test-title-en");
        }

        @Test
        @DisplayName("should normalize a new slug")
        void shouldNormalizeNewSlug() {
            given(postRepository.findById(postId)).willReturn(Optional.of(samplePost));
            given(postRepository.existsBySlug("my-slug")).willReturn(false);
            given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));
            PostUpdateRequest request = new PostUpdateRequest();
            request.setSlug(" My Slug ");

            postService.updatePost(postId, request, "en");

            verify(postRepository).save(postCaptor.capture());
            assertThat(postCaptor.getValue().getSlug()).isEqualTo("my-slug");
        }

        @Test
        @DisplayName("should reject a slug owned by another post")
        void shouldRejectSlugOfAnotherPost() {
            given(postRepository.findById(postId)).willReturn(Optional.of(samplePost));
            given(postRepository.existsBySlug("other-post")).willReturn(true);
            PostUpdateRequest request = new PostUpdateRequest();
            request.setSlug("Other Post");

            assertThatThrownBy(() -> postService.updatePost(postId, request, "en"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Slug already in use");
            verify(postRepository, never()).save(any());
        }

        @Test
        @DisplayName("should accept the post's own unchanged slug")
        void shouldAcceptOwnSlug() {
            given(postRepository.findById(postId)).willReturn(Optional.of(samplePost));
            lenient().when(postRepository.existsBySlug("test-title-en")).thenReturn(true);
            given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));
            PostUpdateRequest request = new PostUpdateRequest();
            request.setSlug("test-title-en");

            postService.updatePost(postId, request, "en");

            verify(postRepository).save(postCaptor.capture());
            assertThat(postCaptor.getValue().getSlug()).isEqualTo("test-title-en");
        }

        @ParameterizedTest
        @ValueSource(strings = {"Kampania-Łódź", "My Post", "My-Post"})
        @DisplayName("should keep a legacy non-canonical slug when it is sent back unchanged")
        void shouldKeepLegacySlugWhenUnchanged(String legacySlug) {
            samplePost.setSlug(legacySlug);
            given(postRepository.findById(postId)).willReturn(Optional.of(samplePost));
            given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));
            PostUpdateRequest request = new PostUpdateRequest();
            request.setSlug(legacySlug);
            request.setExcerptEn("Changed excerpt");

            postService.updatePost(postId, request, "en");

            verify(postRepository).save(postCaptor.capture());
            assertThat(postCaptor.getValue().getSlug()).isEqualTo(legacySlug);
            verify(postRepository, never()).existsBySlug(anyString());
        }
    }

    @Nested
    @DisplayName("response shape")
    class ResponseShape {

        @BeforeEach
        void fillBothLanguages() {
            samplePost.setContentEn(Map.of("type", "doc-en"));
            samplePost.setContentPl(Map.of("type", "doc-pl"));
            samplePost.setCaseStudyChallengeEn("Challenge EN");
            samplePost.setCaseStudyChallengePl("Wyzwanie PL");
            samplePost.setCaseStudySolutionEn("Solution EN");
            samplePost.setCaseStudySolutionPl("Rozwiązanie PL");
            samplePost.setCaseStudyResultsEn("Results EN");
            samplePost.setCaseStudyResultsPl("Wyniki PL");
            samplePost.setCaseStudyTestimonialEn("Testimonial EN");
            samplePost.setCaseStudyTestimonialPl("Opinia PL");
            samplePost.setCaseStudyTestimonialAuthor("Jane Doe");
        }

        @Test
        @DisplayName("public lists should carry only the resolved title and excerpt, without case-study texts")
        void publicListIsLean() {
            given(postRepository.findAllPublished()).willReturn(List.of(samplePost));
            given(postRepository.findByCategoryAndPublishedTrueOrderByDisplayOrderAsc(Category.PERSONAL_PROJECT))
                    .willReturn(List.of(samplePost));
            given(postRepository.findByHashtagAndPublishedTrue("seo")).willReturn(List.of(samplePost));

            List<PostDto> lists = List.of(
                    postService.getAllPublishedPosts("pl").get(0),
                    postService.getPostsByCategory(Category.PERSONAL_PROJECT, "pl").get(0),
                    postService.getPostsByHashtag("seo", "pl").get(0));

            assertThat(lists).allSatisfy(dto -> {
                assertThat(dto.getTitle()).isEqualTo("Test Title PL");
                assertThat(dto.getExcerpt()).isEqualTo("Excerpt PL");
                assertThat(dto.getHasCaseStudy()).isTrue();
                assertThat(dto).extracting(
                                PostDto::getTitleEn, PostDto::getTitlePl,
                                PostDto::getExcerptEn, PostDto::getExcerptPl,
                                PostDto::getContent, PostDto::getContentEn, PostDto::getContentPl,
                                PostDto::getCaseStudyChallenge, PostDto::getCaseStudyChallengeEn,
                                PostDto::getCaseStudyChallengePl,
                                PostDto::getCaseStudySolution, PostDto::getCaseStudySolutionEn,
                                PostDto::getCaseStudySolutionPl,
                                PostDto::getCaseStudyResults, PostDto::getCaseStudyResultsEn,
                                PostDto::getCaseStudyResultsPl,
                                PostDto::getCaseStudyTestimonial, PostDto::getCaseStudyTestimonialEn,
                                PostDto::getCaseStudyTestimonialPl, PostDto::getCaseStudyTestimonialAuthor)
                        .containsOnlyNulls();
            });
        }

        @Test
        @DisplayName("public detail should carry the resolved content and case study but no raw language fields")
        void publicDetailHasOnlyResolvedFields() {
            samplePost.setPublished(true);
            given(postRepository.findBySlug("test-title-en")).willReturn(Optional.of(samplePost));

            PostDto dto = postService.getPostBySlug("test-title-en", "pl");

            assertThat(dto.getContent()).isEqualTo(Map.of("type", "doc-pl"));
            assertThat(dto).extracting(
                            PostDto::getCaseStudyChallenge, PostDto::getCaseStudySolution,
                            PostDto::getCaseStudyResults, PostDto::getCaseStudyTestimonial,
                            PostDto::getCaseStudyTestimonialAuthor)
                    .containsExactly("Wyzwanie PL", "Rozwiązanie PL", "Wyniki PL", "Opinia PL", "Jane Doe");
            assertThat(dto).extracting(
                            PostDto::getTitleEn, PostDto::getTitlePl,
                            PostDto::getExcerptEn, PostDto::getExcerptPl,
                            PostDto::getContentEn, PostDto::getContentPl,
                            PostDto::getCaseStudyChallengeEn, PostDto::getCaseStudyChallengePl,
                            PostDto::getCaseStudySolutionEn, PostDto::getCaseStudySolutionPl,
                            PostDto::getCaseStudyResultsEn, PostDto::getCaseStudyResultsPl,
                            PostDto::getCaseStudyTestimonialEn, PostDto::getCaseStudyTestimonialPl)
                    .containsOnlyNulls();
        }

        @Test
        @DisplayName("admin detail should keep every raw language field")
        void adminDetailKeepsRawFields() {
            given(postRepository.findById(postId)).willReturn(Optional.of(samplePost));

            PostDto dto = postService.getPostById(postId, "en");

            assertThat(dto).extracting(
                            PostDto::getTitleEn, PostDto::getTitlePl,
                            PostDto::getExcerptEn, PostDto::getExcerptPl,
                            PostDto::getCaseStudyChallengeEn, PostDto::getCaseStudyChallengePl,
                            PostDto::getCaseStudySolutionEn, PostDto::getCaseStudySolutionPl,
                            PostDto::getCaseStudyResultsEn, PostDto::getCaseStudyResultsPl,
                            PostDto::getCaseStudyTestimonialEn, PostDto::getCaseStudyTestimonialPl,
                            PostDto::getCaseStudyTestimonialAuthor)
                    .containsExactly(
                            "Test Title EN", "Test Title PL", "Excerpt EN", "Excerpt PL",
                            "Challenge EN", "Wyzwanie PL", "Solution EN", "Rozwiązanie PL",
                            "Results EN", "Wyniki PL", "Testimonial EN", "Opinia PL", "Jane Doe");
            assertThat(dto.getContentEn()).isEqualTo(Map.of("type", "doc-en"));
            assertThat(dto.getContentPl()).isEqualTo(Map.of("type", "doc-pl"));
        }

        @Test
        @DisplayName("admin list should keep every raw language field")
        void adminListKeepsRawFields() {
            given(postRepository.findAll()).willReturn(List.of(samplePost));
            given(postRepository.findAllByCategory(Category.PERSONAL_PROJECT)).willReturn(List.of(samplePost));

            List<PostDto> lists = List.of(
                    postService.getAllPosts("en").get(0),
                    postService.getAllPostsByCategory(Category.PERSONAL_PROJECT, "en").get(0));

            assertThat(lists).allSatisfy(dto -> assertThat(dto).extracting(
                            PostDto::getTitleEn, PostDto::getTitlePl,
                            PostDto::getExcerptEn, PostDto::getExcerptPl,
                            PostDto::getCaseStudyChallengeEn, PostDto::getCaseStudyChallengePl,
                            PostDto::getCaseStudyTestimonialAuthor)
                    .containsExactly("Test Title EN", "Test Title PL", "Excerpt EN", "Excerpt PL",
                            "Challenge EN", "Wyzwanie PL", "Jane Doe"));
        }
    }
}
