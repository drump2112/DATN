package com.example.DATN.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SaleEventProductDTO {
    private Integer id;
    private Integer salesEventId;
    private Integer productVariantId;
    private String productVariantName;
    private BigDecimal finalPrice;
}