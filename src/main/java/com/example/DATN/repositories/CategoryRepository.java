package com.example.DATN.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.example.DATN.models.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    List<Category> findByNameContainingIgnoreCase(String name);


    @Query("SELECT COALESCE(MAX(s.id), 0) FROM Category s")
    Long findMaxId();


    @Query("""
       SELECT s FROM Category s
       WHERE (:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(s.cateCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
         AND (:isActive IS NULL OR s.isActive = :isActive)
   """)
    Page<Category> search(@Param("keyword") String keyword,
                          @Param("isActive") Boolean isActive,
                          Pageable pageable);


    boolean existsByName(String name);

}
