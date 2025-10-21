package com.example.DATN.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "SaleEventProducts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleEventProduct {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id;


   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "SaleEventID")
   private SalesEvent salesEvent;


   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "ProductVariantID")
   private ProductVariant productVariant;


   @Column(name = "FinalPrice", precision = 10, scale = 2)
   private BigDecimal finalPrice;
}

