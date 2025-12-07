        package com.example.DATN.repositories;

import java.util.List;


import com.example.DATN.models.Size;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SizeRepository extends JpaRepository<Size, Integer> {
    List<Size> findByNameContainingIgnoreCase(String name);


    long countAllById(Integer id);


    @Query("SELECT COALESCE(MAX(s.id), 0) FROM Size s")
    Long findMaxId();


    @Query("""
       SELECT s FROM Size s
       WHERE (:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(s.sizeCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
         AND (:isActive IS NULL OR s.isActive = :isActive)
   """)
    Page<Size> search(@Param("keyword") String keyword,
                      @Param("isActive") Boolean isActive,
                      Pageable pageable);


    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Integer id);


}
