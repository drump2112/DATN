package com.example.DATN.repositories;

import java.util.List;
import java.util.Optional;

import com.example.DATN.models.Brand;

import com.example.DATN.models.Color;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BrandRepository extends JpaRepository<Brand, Integer> {

	Optional<Brand> findTopByOrderByIdDesc();

	List<Brand> findByNameContainingIgnoreCase(String name);

    @Query("""
                SELECT s FROM Brand s
                WHERE (:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                                     OR LOWER(s.brandCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
                  AND (:isActive IS NULL OR s.isActive = :isActive)
            """)
    Page<Brand> search(@Param("keyword") String keyword,
                       @Param("isActive") Boolean isActive,
                       Pageable pageable);
}
