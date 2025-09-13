package com.example.DATN.repositories;

import java.util.List;

import com.example.DATN.models.Color;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ColorRepoSitory extends JpaRepository<Color, Integer> {
    List<Color> findByNameContainingIgnoreCase(String name);

    @Query("SELECT COALESCE(MAX(s.id), 0) FROM Color s")
    Long findMaxId();

    @Query("""
                SELECT s FROM Color s
                WHERE (:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                                     OR LOWER(s.colorCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
                  AND (:isActive IS NULL OR s.isActive = :isActive)
            """)
    Page<Color> search(@Param("keyword") String keyword,
                       @Param("isActive") Boolean isActive,
                       Pageable pageable);

    boolean existsByName(String name);
}

