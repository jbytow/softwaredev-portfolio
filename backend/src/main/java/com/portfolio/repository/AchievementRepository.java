package com.portfolio.repository;

import com.portfolio.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, UUID> {

    List<Achievement> findAllByOrderByDisplayOrderAsc();

    @Query("SELECT COALESCE(MAX(a.displayOrder), 0) FROM Achievement a")
    Integer getMaxDisplayOrder();
}
