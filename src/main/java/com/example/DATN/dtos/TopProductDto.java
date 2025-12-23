package com.example.DATN.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductDto {
    private Integer productId;
    private String productName;
    private String productCode;
    private Long totalSold;
    private String colorName;
    private String colorCode;
    private String variantImageUrl;
    private String thumbnail;
}