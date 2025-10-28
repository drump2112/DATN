package com.example.DATN.request;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VoucherRequest {
   private Integer id;

   private String name;

   private String code;

   private String discountType;

   private BigDecimal discountValue;

   private BigDecimal minOrderAmount;

   private BigDecimal maxDiscountValue;

   // @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)

   private LocalDateTime startDate;

   // @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)

   private LocalDateTime endDate;

   private Integer quantity;

   private Boolean isActive;
}
