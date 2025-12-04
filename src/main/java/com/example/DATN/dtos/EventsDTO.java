package com.example.DATN.dtos;


import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventsDTO {

   private Integer id;

   private String name;

   private String code;

   @DateTimeFormat(pattern = "yyyy-MM-dd")
   private LocalDateTime startDate;

   @DateTimeFormat(pattern = "yyyy-MM-dd")
   private LocalDateTime endDate;

   private String discountType;

   private BigDecimal discountValue;

   private BigDecimal maxDiscountValue;

   private Boolean isActive;

   private List<SaleEventProductDTO> saleEventProducts;

   private List<Integer> productVariantIds;
}

