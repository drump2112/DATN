package com.example.DATN.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

import com.example.DATN.dtos.CartItemDTO;

@Data
public class OrderRequest {

  private Integer userId;

  private String paymentMethod;

  private String customerName;

  private String shippingAddress;

  private String shippingPhone;

  private BigDecimal shippingFee;

  private String voucherCode;

  private Integer voucherId;

  private BigDecimal totalAmount;

  private BigDecimal discountAmount;

  private BigDecimal finalAmount;

  private List<CartItemDTO> items;
}
