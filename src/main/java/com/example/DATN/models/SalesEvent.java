package com.example.DATN.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "SalesEvent")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesEvent {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id;
   @Column(name = "Name", length = 100)
   private String name;

   @Column(name = "Code", length = 50)
   private String code;

   @Column(name = "StartDate")
   private LocalDateTime startDate;

   @Column(name = "EndDate")
   private LocalDateTime endDate;

   @Column(name = "DiscountType", length = 10)
   private String discountType;

   @Column(name = "DiscountValue", precision = 10, scale = 2)
   private BigDecimal discountValue;

   @Column(name = "MaxDiscountValue", precision = 10, scale = 2)
   private BigDecimal maxDiscountValue;

   @Column(name = "IsActive")
   private Boolean isActive;

   @OneToMany(mappedBy = "salesEvent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
   private List<SaleEventProduct> saleEventProducts;
}

