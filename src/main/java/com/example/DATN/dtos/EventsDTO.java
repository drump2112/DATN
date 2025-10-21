package com.example.DATN.dtos;


import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;




@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventsDTO {

   private Integer id;

   private String name;

   private String code;

   @DateTimeFormat(pattern = "yyyy-MM-dd")  // Parse "2025-10-14" → 2025-10-14T00:00 (default time 00:00)
   private LocalDateTime startDate;

   @DateTimeFormat(pattern = "yyyy-MM-dd")
   private LocalDateTime endDate;

   private String discountType;

   private BigDecimal discountValue;

   private BigDecimal maxDiscountValue;

   private Boolean isActive;

//chưa có chức năng tạo sự kiện giảm giá theo sản phẩm
//    private List<SaleEventProduct> saleEventProducts;
}

