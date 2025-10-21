package com.example.DATN.request;


import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;


import java.math.BigDecimal;
import java.time.LocalDate;


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
   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  // Tự parse "YYYY-MM-DD" → LocalDate
   private LocalDate startDate;  // Đổi type
   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
   private LocalDate endDate;    // Đổi type
   private Integer quantity;
   private Boolean isActive;
}

