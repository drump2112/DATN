package com.example.DATN.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private Integer id;
    private String orderCode;
    private String customerName;
    private String userCode;
    private String shippingPhone;
    private String shippingAddress;
    private LocalDateTime orderDate;

    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private BigDecimal shippingFee;

    private String orderType;
    private String paymentMethod;
    private String status;
    private String voucherCode;

    private List<OrderItemDTO> items;
}