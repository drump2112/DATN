package com.example.DATN.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.DATN.models.StockMovement;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Integer> {

    /**
     * Lấy lịch sử chuyển động theo product variant, sắp xếp theo thời gian mới nhất
     */
    List<StockMovement> findByProductVariantIdOrderByCreatedAtDesc(Integer productVariantId);

    /**
     * Lấy lịch sử chuyển động theo product variant với phân trang
     */
    Page<StockMovement> findByProductVariantIdOrderByCreatedAtDesc(Integer productVariantId, Pageable pageable);

    /**
     * Lấy chuyển động theo loại
     */
    Page<StockMovement> findByMovementTypeOrderByCreatedAtDesc(String movementType, Pageable pageable);

    /**
     * Lấy chuyển động theo product variant và loại
     */
    List<StockMovement> findByProductVariantIdAndMovementTypeOrderByCreatedAtDesc(
        Integer productVariantId, String movementType);

    /**
     * Lấy tổng số lượng nhập theo product variant
     */
    @Query("SELECT COALESCE(SUM(sm.quantity), 0) FROM StockMovement sm " +
           "WHERE sm.productVariant.id = :productVariantId " +
           "AND sm.movementType = 'IN'")
    Integer getTotalStockIn(@Param("productVariantId") Integer productVariantId);

    /**
     * Lấy tổng số lượng xuất theo product variant
     */
    @Query("SELECT COALESCE(SUM(sm.quantity), 0) FROM StockMovement sm " +
           "WHERE sm.productVariant.id = :productVariantId " +
           "AND sm.movementType IN ('OUT', 'SALE', 'DAMAGE')")
    Integer getTotalStockOut(@Param("productVariantId") Integer productVariantId);

    /**
     * Lấy chuyển động trong khoảng thời gian
     */
    @Query("SELECT sm FROM StockMovement sm " +
           "WHERE sm.createdAt >= :startDate AND sm.createdAt <= :endDate " +
           "ORDER BY sm.createdAt DESC")
    Page<StockMovement> findMovementsByDateRange(
        @Param("startDate") java.time.LocalDateTime startDate,
        @Param("endDate") java.time.LocalDateTime endDate,
        Pageable pageable);

    /**
     * Lấy chuyển động theo người tạo
     */
    Page<StockMovement> findByCreatedByOrderByCreatedAtDesc(String createdBy, Pageable pageable);
}