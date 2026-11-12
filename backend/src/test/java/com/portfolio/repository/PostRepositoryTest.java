package com.portfolio.repository;

import com.portfolio.config.TestContainersConfig;
import com.portfolio.entity.Category;
import com.portfolio.entity.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.Query;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for PostRepository using TestContainers, against the schema built by Flyway.
 * Requires Docker to be running. Tests will be skipped if Docker is unavailable.
 */
@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainersConfig.class)
@ActiveProfiles("test")
@DisplayName("PostRepository")
class PostRepositoryTest {

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

    @Nested
    @DisplayName("findByHashtagAndPublishedTrue")
    class FindByHashtag {

        @BeforeEach
        void addTaggedPosts() {
            entityManager.persist(taggedPost("tagged-published", true, 10, "seo", "ads"));
            entityManager.persist(taggedPost("tagged-draft", false, 11, "seo"));
            entityManager.persist(taggedPost("tagged-other", true, 12, "other"));
            entityManager.flush();
        }

        @Test
        @DisplayName("should return only published posts carrying the tag")
        void shouldReturnOnlyPublishedPostsWithTag() {
            List<Post> result = postRepository.findByHashtagAndPublishedTrue("seo");

            assertThat(result).extracting(Post::getSlug).containsExactly("tagged-published");
        }

        @Test
        @DisplayName("should match a tag that is not the first array element")
        void shouldMatchAnyElement() {
            List<Post> result = postRepository.findByHashtagAndPublishedTrue("ads");

            assertThat(result).extracting(Post::getSlug).containsExactly("tagged-published");
        }

        @Test
        @DisplayName("should return empty list for a tag no post carries")
        void shouldReturnEmptyForUnknownTag() {
            assertThat(postRepository.findByHashtagAndPublishedTrue("missing")).isEmpty();
        }

        @Test
        @DisplayName("should be answerable from the GIN index on hashtags")
        void shouldUseGinIndex() throws NoSuchMethodException {
            String sql = PostRepository.class
                    .getMethod("findByHashtagAndPublishedTrue", String.class)
                    .getAnnotation(Query.class).value()
                    .replace(":hashtag", "'seo'");
            // Give the planner realistic statistics (many published posts, few with the tag),
            // otherwise it picks another index for any predicate; seq scan is disabled
            // because on a small table it always wins and would hide whether GIN is usable.
            entityManager.getEntityManager().createNativeQuery(
                    "INSERT INTO posts (id, category, title_en, title_pl, slug, published, display_order, hashtags) "
                            + "SELECT gen_random_uuid(), 'PERSONAL_PROJECT', 'filler', 'filler', 'filler-' || n, true, n, "
                            + "ARRAY['filler'] FROM generate_series(1, 2000) AS n")
                    .executeUpdate();
            entityManager.getEntityManager().createNativeQuery("ANALYZE posts").executeUpdate();
            entityManager.getEntityManager()
                    .createNativeQuery("SELECT set_config('enable_seqscan', 'off', true)")
                    .getSingleResult();

            @SuppressWarnings("unchecked")
            List<Object> plan = entityManager.getEntityManager()
                    .createNativeQuery("EXPLAIN " + sql)
                    .getResultList();

            assertThat(plan.stream().map(String::valueOf).collect(Collectors.joining(System.lineSeparator())))
                    .contains("idx_posts_hashtags");
        }

        private Post taggedPost(String slug, boolean published, int displayOrder, String... hashtags) {
            Post post = Post.builder()
                    .category(Category.PERSONAL_PROJECT)
                    .titleEn(slug)
                    .titlePl(slug)
                    .slug(slug)
                    .published(published)
                    .displayOrder(displayOrder)
                    .build();
            post.setHashtags(hashtags);
            return post;
        }
    }
}
