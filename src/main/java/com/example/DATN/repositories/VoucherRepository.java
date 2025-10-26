package com.example.DATN.repositories;

import com.example.DATN.models.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface VoucherRepository extends JpaRepository<Voucher, Integer> {

        Voucher findByCode(String code);

        List<Voucher> findByCodeContainingIgnoreCase(String name);

        @Query("SELECT COALESCE(MAX(s.id), 0) FROM Voucher s")
        Long findMaxId();

        @Query("SELECT v FROM Voucher v WHERE " +
                        "(:keyword IS NULL OR LOWER(v.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "OR LOWER(v.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                        "AND (:isActive IS NULL OR v.isActive = :isActive)")
        Page<Voucher> findBySearch(@Param("keyword") String keyword,
                        @Param("isActive") Boolean isActive,
                        Pageable pageable);

        @Query("SELECT v FROM Voucher v " +
                        "WHERE v.isActive = true " +
                        "AND v.startDate <= :now " +
                        "AND v.endDate >= :now " +
                        "AND (v.minOrderAmount IS NULL OR v.minOrderAmount <= :orderTotal)")
        List<Voucher> findValidVouchers(@Param("now") LocalDateTime now,
                        @Param("orderTotal") BigDecimal orderTotal);
}
