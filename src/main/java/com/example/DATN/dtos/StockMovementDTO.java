package com.example.DATN.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockMovementDTO {

    private Integer id;
    private Integer productVariantId;
    private String variantCode;
    private String productName;
    private String colorName;
    private String sizeName;
    private Integer quantity;
    private String movementType;
    private String note;
    private LocalDateTime createdAt;
    private String createdBy;

    // Thông tin bổ sung cho hiển thị
    private Integer currentStock; // Số lượng hiện tại sau khi thay đổi
    private Integer previousStock; // Số lượng trước khi thay đổi
    private String movementTypeDisplay; // Hiển thị loại chuyển động bằng tiếng Việt
}