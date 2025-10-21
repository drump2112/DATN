package com.example.DATN.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class VoucherDTO {

   private Integer id;

   private String name;

   private String code;

   private String discountType;

   private BigDecimal discountValue;

   private BigDecimal minOrderAmount;

   private BigDecimal maxDiscountValue;

   @DateTimeFormat(pattern = "yyyy-MM-dd")
   private LocalDateTime startDate;

   @DateTimeFormat(pattern = "yyyy-MM-dd")
   private LocalDateTime endDate;

   private Integer quantity;

   private Boolean isActive;
}
