package com.example.DATN.request;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder
public class EventsRequest {

  private Integer id;

  private String name;

  private String code;

  @DateTimeFormat(pattern = "yyyy-MM-dd") // Parse "2025-10-14" → 2025-10-14T00:00 (default time 00:00)
  private LocalDate startDate;

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate endDate;

  private String discountType;

  private BigDecimal discountValue;

  private BigDecimal maxDiscountValue;

  private Boolean isActive;

  private List<Integer> productVariantIds; // IDs của ProductVariant được chọn
}
