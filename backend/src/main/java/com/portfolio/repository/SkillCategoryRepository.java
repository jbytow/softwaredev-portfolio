package com.portfolio.repository;

import com.portfolio.entity.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SkillCategoryRepository extends JpaRepository<SkillCategory, UUID> {

    List<SkillCategory> findAllByOrderByDisplayOrderAsc();

    @Query("SELECT COALESCE(MAX(c.displayOrder), 0) FROM SkillCategory c")
    int getMaxDisplayOrder();

    @Modifying
    @Query("UPDATE SkillCategory c SET c.displayOrder = :displayOrder WHERE c.id = :id")
    void updateDisplayOrder(@Param("id") UUID id, @Param("displayOrder") int displayOrder);
}
