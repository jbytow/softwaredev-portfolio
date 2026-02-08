package com.portfolio.repository;

import com.portfolio.entity.Category;
import com.portfolio.entity.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration tests for PostRepository using TestContainers.
 * Requires Docker to be running. Tests will be skipped if Docker is unavailable.
 */
@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("PostRepository")
class PostRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.flyway.enabled", () -> false);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PostRepository postRepository;

    private Post publishedPost1;
    private Post publishedPost2;
    private Post draftPost;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();

        publishedPost1 = Post.builder()
                .category(Category.PERSONAL_PROJECT)
                .titleEn("Published Post 1")
                .titlePl("Opublikowany Post 1")
                .slug("published-post-1")
                .published(true)
                .displayOrder(1)
                .build();

        publishedPost2 = Post.builder()
                .category(Category.PERSONAL_PROJECT)
                .titleEn("Published Post 2")
                .titlePl("Opublikowany Post 2")
                .slug("published-post-2")
                .published(true)
                .displayOrder(2)
                .build();

        draftPost = Post.builder()
                .category(Category.PERSONAL_PROJECT)
                .titleEn("Draft Post")
                .titlePl("Szkic Postu")
                .slug("draft-post")
                .published(false)
                .displayOrder(3)
                .build();

        entityManager.persist(publishedPost1);
        entityManager.persist(publishedPost2);
        entityManager.persist(draftPost);
        entityManager.flush();
    }

    @Nested
    @DisplayName("findBySlug")
    class FindBySlug {

        @Test
        @DisplayName("should return post when slug exists")
        void shouldReturnPostWhenSlugExists() {
            Optional<Post> result = postRepository.findBySlug("published-post-1");

            assertThat(result).isPresent();
            assertThat(result.get().getTitleEn()).isEqualTo("Published Post 1");
        }

        @Test
        @DisplayName("should return empty when slug does not exist")
        void shouldReturnEmptyWhenSlugNotExists() {
            Optional<Post> result = postRepository.findBySlug("non-existent-slug");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllPublished")
    class FindAllPublished {

        @Test
        @DisplayName("should return only published posts")
        void shouldReturnOnlyPublishedPosts() {
            List<Post> result = postRepository.findAllPublished();

            assertThat(result).hasSize(2);
            assertThat(result).allMatch(Post::getPublished);
        }

        @Test
        @DisplayName("should not include draft posts")
        void shouldNotIncludeDraftPosts() {
            List<Post> result = postRepository.findAllPublished();

            assertThat(result).noneMatch(p -> p.getSlug().equals("draft-post"));
        }

        @Test
        @DisplayName("should order by displayOrder ascending")
        void shouldOrderByDisplayOrder() {
            List<Post> result = postRepository.findAllPublished();

            assertThat(result).isSortedAccordingTo(
                    (p1, p2) -> Integer.compare(p1.getDisplayOrder(), p2.getDisplayOrder())
            );
        }
    }

    @Nested
    @DisplayName("findByPublishedTrue")
    class FindByPublishedTrue {

        @Test
        @DisplayName("should return paginated published posts")
        void shouldReturnPaginatedPublishedPosts() {
            Page<Post> result = postRepository.findByPublishedTrue(PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("should respect page size")
        void shouldRespectPageSize() {
            Page<Post> result = postRepository.findByPublishedTrue(PageRequest.of(0, 1));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalPages()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("findByCategoryAndPublishedTrueOrderByDisplayOrderAsc")
    class FindByCategoryAndPublished {

        @Test
        @DisplayName("should return published posts for category")
        void shouldReturnPublishedPostsForCategory() {
            List<Post> result = postRepository
                    .findByCategoryAndPublishedTrueOrderByDisplayOrderAsc(Category.PERSONAL_PROJECT);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getSlug()).isEqualTo("published-post-1");
            assertThat(result.get(1).getSlug()).isEqualTo("published-post-2");
        }

        @Test
        @DisplayName("should not return draft posts for category")
        void shouldNotReturnDraftPostsForCategory() {
            List<Post> result = postRepository
                    .findByCategoryAndPublishedTrueOrderByDisplayOrderAsc(Category.PERSONAL_PROJECT);

            assertThat(result).noneMatch(p -> p.getSlug().equals("draft-post"));
        }

        @Test
        @DisplayName("should return empty list for category with no published posts")
        void shouldReturnEmptyForCategoryWithNoPublishedPosts() {
            List<Post> result = postRepository
                    .findByCategoryAndPublishedTrueOrderByDisplayOrderAsc(Category.PROFESSIONAL_PROJECT);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllByCategory")
    class FindAllByCategory {

        @Test
        @DisplayName("should return all posts for category including drafts")
        void shouldReturnAllPostsIncludingDrafts() {
            List<Post> result = postRepository.findAllByCategory(Category.PERSONAL_PROJECT);

            assertThat(result).hasSize(3);
            assertThat(result).extracting(Post::getSlug)
                    .containsExactlyInAnyOrder("published-post-1", "published-post-2", "draft-post");
        }
    }

    @Nested
    @DisplayName("countByCategoryAndPublished")
    class CountByCategoryAndPublished {

        @Test
        @DisplayName("should count only published posts in category")
        void shouldCountOnlyPublishedInCategory() {
            long count = postRepository.countByCategoryAndPublished(Category.PERSONAL_PROJECT);

            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("countPublished")
    class CountPublished {

        @Test
        @DisplayName("should count all published posts")
        void shouldCountAllPublished() {
            long count = postRepository.countPublished();

            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("existsBySlug")
    class ExistsBySlug {

        @Test
        @DisplayName("should return true when slug exists")
        void shouldReturnTrueWhenExists() {
            boolean exists = postRepository.existsBySlug("published-post-1");

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("should return false when slug does not exist")
        void shouldReturnFalseWhenNotExists() {
            boolean exists = postRepository.existsBySlug("non-existent");

            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("getMaxDisplayOrder")
    class GetMaxDisplayOrder {

        @Test
        @DisplayName("should return max display order for category")
        void shouldReturnMaxDisplayOrder() {
            int maxOrder = postRepository.getMaxDisplayOrder(Category.PERSONAL_PROJECT);

            assertThat(maxOrder).isEqualTo(3); // draft-post has displayOrder 3
        }

        @Test
        @DisplayName("should return 0 when no posts in category")
        void shouldReturnZeroWhenNoPostsInCategory() {
            int maxOrder = postRepository.getMaxDisplayOrder(Category.PROFESSIONAL_PROJECT);

            assertThat(maxOrder).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("updateDisplayOrder")
    class UpdateDisplayOrder {

        @Test
        @DisplayName("should update display order")
        void shouldUpdateDisplayOrder() {
            postRepository.updateDisplayOrder(publishedPost1.getId(), 99);
            entityManager.clear();

            Post updated = postRepository.findById(publishedPost1.getId()).orElseThrow();

            assertThat(updated.getDisplayOrder()).isEqualTo(99);
        }
    }
}
