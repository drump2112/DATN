package com.example.DATN.repositories;

import com.example.DATN.models.SalesEvent;
import com.example.DATN.models.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventRepository extends JpaRepository<SalesEvent, Integer> {
  SalesEvent findByCode(String code);

  List<SalesEvent> findByCodeContainingIgnoreCase(String name);

  @Query("SELECT COALESCE(MAX(s.id), 0) FROM SalesEvent s")
  Long findMaxId();

  @Query("SELECT v FROM SalesEvent v WHERE " +
      "(:keyword IS NULL OR LOWER(v.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
      "OR LOWER(v.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
      "AND (:isActive IS NULL OR v.isActive = :isActive)")
  Page<SalesEvent> findBySearch(@Param("keyword") String keyword,
      @Param("isActive") Boolean isActive,
      Pageable pageable);
}
