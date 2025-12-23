package com.example.DATN.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

import com.example.DATN.dtos.OrderItemDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CounterOrderRequest {

  private Integer userId;

  private String paymentMethod;

  private List<OrderItemDTO> items;

  private BigDecimal discountAmount;

  private Integer voucherId;

  // Flag để đánh dấu tồn kho đã được trừ trước (từ giỏ hàng offline)
  private boolean stockAlreadyDeducted;

  // Session ID để xóa thông tin giữ tồn kho sau khi tạo đơn thành công
  private String offlineCartSessionId;
}
