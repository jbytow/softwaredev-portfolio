package com.portfolio.repository;

import com.portfolio.entity.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, UUID> {

    List<Experience> findAllByOrderByDisplayOrderAsc();

    List<Experience> findAllByOrderByStartDateDesc();

    @Query("SELECT COALESCE(MAX(e.displayOrder), 0) FROM Experience e")
    int getMaxDisplayOrder();

    @Modifying
    @Query("UPDATE Experience e SET e.displayOrder = :displayOrder WHERE e.id = :id")
    void updateDisplayOrder(@Param("id") UUID id, @Param("displayOrder") int displayOrder);
}
