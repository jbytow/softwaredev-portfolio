package com.portfolio.repository;

import com.portfolio.entity.Category;
import com.portfolio.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    Optional<Post> findBySlug(String slug);

    List<Post> findByPublishedTrueOrderByDisplayOrderAsc();

    List<Post> findByCategoryAndPublishedTrueOrderByDisplayOrderAsc(Category category);

    List<Post> findByCategoryOrderByDisplayOrderAsc(Category category);

    @Query("SELECT p FROM Post p WHERE p.published = true ORDER BY p.displayOrder ASC")
    List<Post> findAllPublished();

    @Query("SELECT p FROM Post p WHERE p.category = :category ORDER BY p.displayOrder ASC")
    List<Post> findAllByCategory(@Param("category") Category category);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.category = :category AND p.published = true")
    long countByCategoryAndPublished(@Param("category") Category category);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.published = true")
    long countPublished();

    @Query("SELECT COALESCE(MAX(p.displayOrder), 0) FROM Post p WHERE p.category = :category")
    int getMaxDisplayOrder(@Param("category") Category category);

    boolean existsBySlug(String slug);

    @Modifying
    @Query("UPDATE Post p SET p.displayOrder = :displayOrder WHERE p.id = :id")
    void updateDisplayOrder(@Param("id") UUID id, @Param("displayOrder") int displayOrder);

    // Hashtag methods
    // "@>" rather than "= ANY": only the containment form can use the GIN index idx_posts_hashtags.
    @Query(value = "SELECT * FROM posts WHERE published = true AND hashtags @> ARRAY[CAST(:hashtag AS text)] ORDER BY display_order", nativeQuery = true)
    List<Post> findByHashtagAndPublishedTrue(@Param("hashtag") String hashtag);
}
