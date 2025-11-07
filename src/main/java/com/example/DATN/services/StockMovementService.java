package com.example.DATN.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.DATN.dtos.StockMovementDTO;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.models.StockMovement;

public interface StockMovementService {

    /**
     * Ghi lại chuyển động kho
     */
    StockMovement recordStockMovement(Integer productVariantId, Integer quantity,
                                    String movementType, String note, String createdBy);

    /**
     * Ghi lại chuyển động kho với ProductVariant object
     */
    StockMovement recordStockMovement(ProductVariant productVariant, Integer quantity,
                                    String movementType, String note, String createdBy);

    /**
     * Lấy lịch sử chuyển động của một product variant
     */
    List<StockMovementDTO> getStockMovementHistory(Integer productVariantId);

    /**
     * Lấy lịch sử chuyển động với phân trang
     */
    Page<StockMovementDTO> getStockMovementHistory(Integer productVariantId, Pageable pageable);

    /**
     * Lấy tất cả chuyển động kho với phân trang
     */
    Page<StockMovementDTO> getAllStockMovements(Pageable pageable);

    /**
     * Lấy chuyển động kho theo loại
     */
    Page<StockMovementDTO> getStockMovementsByType(String movementType, Pageable pageable);

    /**
     * Cập nhật số lượng và ghi lại chuyển động
     */
    void updateStockWithMovement(Integer productVariantId, Integer newQuantity,
                               String movementType, String note, String createdBy);

    /**
     * Xử lý bán hàng (trừ kho)
     */
    void processSale(Integer productVariantId, Integer quantity, String orderCode, String createdBy);

    /**
     * Xử lý hoàn trả (cộng kho)
     */
    void processReturn(Integer productVariantId, Integer quantity, String orderCode, String createdBy);

    /**
     * Xử lý nhập kho
     */
    void processStockIn(Integer productVariantId, Integer quantity, String note, String createdBy);

    /**
     * Xử lý xuất kho
     */
    void processStockOut(Integer productVariantId, Integer quantity, String note, String createdBy);

    /**
     * Xử lý hàng hỏng
     */
    void processDamage(Integer productVariantId, Integer quantity, String note, String createdBy);

    /**
     * Xử lý cập nhật số lượng thủ công
     */
    void processManualUpdate(Integer productVariantId, Integer oldQuantity, Integer newQuantity, String note, String createdBy);
}