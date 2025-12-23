package com.example.DATN.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuarterlyProductSalesDto {
    private Integer quarter;
    private Integer year;
    private String productName;
    private Long totalSold;
}