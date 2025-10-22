package com.example.DATN.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId")
    private User user;

    @Column(name = "OrderDate")
    private LocalDateTime orderDate = LocalDateTime.now();

    @Column(name = "OrderType")
    private String orderType = "COUNTER"; // Đơn tại quầy

    @Column(name = "Status")
    private String status = "PENDING";

    @Column(name = "OrderCode")
    private String orderCode;

    @Column(name = "PaymentMethod")
    private String paymentMethod;

    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private BigDecimal shippingFee;
    private String shippingAddress;
    private String shippingPhone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VoucherID")
    private Voucher voucher;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<OrderItem> items = new ArrayList<>();
}
