package com.example.DATN.models;

import java.math.BigDecimal;

import jakarta.persistence.*;
import org.hibernate.annotations.DialectOverride.Formula;

import groovy.transform.builder.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OrderItems")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductVariantId")
    private ProductVariant productVariant;

    private Integer quantity;

    private BigDecimal unitPrice;

    @ManyToOne
    @JoinColumn(name = "OrderId")
    private Order order;

    @org.hibernate.annotations.Formula("quantity * unitPrice")
    private BigDecimal totalPrice;

}

