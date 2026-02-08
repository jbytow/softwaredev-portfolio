package com.portfolio.repository;

import com.portfolio.entity.RpgStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RpgStatRepository extends JpaRepository<RpgStat, UUID> {

    List<RpgStat> findAllByOrderByDisplayOrderAsc();

    @Query("SELECT COALESCE(MAX(r.displayOrder), 0) FROM RpgStat r")
    Integer getMaxDisplayOrder();
}
