package com.example.DATN.services;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.DATN.dtos.OrderDTO;
import com.example.DATN.dtos.OrderDetailResponse;
import com.example.DATN.models.Order;
import com.example.DATN.request.CounterOrderRequest;
import com.example.DATN.request.OrderRequest;

public interface OrderService {

  Order createCounterOrder(CounterOrderRequest request);

  OrderDetailResponse getOrderDetailByCode(String orderCode);

  Order createOrder(OrderRequest dto);

  Page<OrderDTO> getOnlineOrders(int page, int size);

  Page<OrderDTO> getOfflineOrders(int page, int size);

  Page<OrderDTO> getUserOrders(Integer userId, int page, int size);

  boolean confirmOtp(Integer orderId, String email, String inputOtp);

  OrderDTO getOrderById(Integer orderId);

  boolean updateOrderStatus(Integer orderId, String status);

  Order findById(Integer orderId);

  void updatePaymentStatus(Order order, String status, String transactionNo);

  void processOrderItems(Order order);
}
