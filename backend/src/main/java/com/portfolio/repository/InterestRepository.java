package com.portfolio.repository;

import com.portfolio.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InterestRepository extends JpaRepository<Interest, UUID> {

    List<Interest> findAllByOrderByDisplayOrderAsc();

    @Query("SELECT COALESCE(MAX(i.displayOrder), 0) FROM Interest i")
    Integer getMaxDisplayOrder();
}
