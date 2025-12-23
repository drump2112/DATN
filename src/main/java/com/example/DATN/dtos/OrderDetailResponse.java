package com.example.DATN.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetailResponse {

    private String orderCode;

    private LocalDateTime orderDate;

    private String orderType;

    private String status;

    private String paymentMethod;

    private String customerName;

    private String customerPhone;

    private String shippingAddress;

    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    private BigDecimal finalAmount;

    private List<OrderItemResponse> items;

}
