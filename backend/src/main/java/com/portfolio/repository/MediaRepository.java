package com.portfolio.repository;

import com.portfolio.entity.Media;
import com.portfolio.entity.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MediaRepository extends JpaRepository<Media, UUID> {

    List<Media> findByPostIdOrderByCreatedAtDesc(UUID postId);

    List<Media> findByPostIdOrderByDisplayOrderAsc(UUID postId);

    Page<Media> findByPostIdIsNull(Pageable pageable);

    Page<Media> findByType(MediaType type, Pageable pageable);

    List<Media> findByPostIdIsNullOrderByCreatedAtDesc();

    boolean existsByFilename(String filename);

    @Query("SELECT COALESCE(MAX(m.displayOrder), 0) FROM Media m WHERE m.post.id = :postId")
    int getMaxDisplayOrderForPost(@Param("postId") UUID postId);

    @Modifying
    @Query("UPDATE Media m SET m.displayOrder = :displayOrder WHERE m.id = :id")
    void updateDisplayOrder(@Param("id") UUID id, @Param("displayOrder") int displayOrder);
}
