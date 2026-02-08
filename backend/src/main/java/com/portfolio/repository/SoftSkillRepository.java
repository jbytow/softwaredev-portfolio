package com.portfolio.repository;

import com.portfolio.entity.SoftSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SoftSkillRepository extends JpaRepository<SoftSkill, UUID> {

    List<SoftSkill> findAllByOrderByDisplayOrderAsc();

    @Query("SELECT COALESCE(MAX(s.displayOrder), 0) FROM SoftSkill s")
    int getMaxDisplayOrder();

    @Modifying
    @Query("UPDATE SoftSkill s SET s.displayOrder = :displayOrder WHERE s.id = :id")
    void updateDisplayOrder(@Param("id") UUID id, @Param("displayOrder") int displayOrder);
}
