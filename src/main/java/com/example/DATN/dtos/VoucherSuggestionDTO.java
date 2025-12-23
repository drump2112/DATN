package com.example.DATN.dtos;

import lombok.*;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VoucherSuggestionDTO {
    private Integer id;
    private String code;
    private String name;
    private String discountType;
    private BigDecimal discountValue;

    private BigDecimal discountAmount;
    private BigDecimal totalBefore;
    private BigDecimal totalAfter;

    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountValue;
}