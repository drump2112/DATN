package com.example.DATN.services;

import java.time.LocalDate;
import java.util.List;

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

  void validateStockBeforeOrder(List<com.example.DATN.dtos.CartItemDTO> items) throws Exception;

  Page<OrderDTO> getOnlineOrders(int page, int size);

  Page<OrderDTO> getOfflineOrders(int page, int size);

  Page<OrderDTO> getUserOrders(Integer userId, int page, int size);

  boolean confirmOtp(Integer orderId, String email, String inputOtp);

  OrderDTO getOrderById(Integer orderId);

  boolean updateOrderStatus(Integer orderId, String status);

  Order findById(Integer orderId);

  void updatePaymentStatus(Order order, String status, String transactionNo);

  void processOrderItems(Order order);

  Page<OrderDTO> searchOnlineOrders(String keyword, String paymentMethod,
                                   LocalDate dateStart, LocalDate dateEnd,
                                   int page, int size);

  Page<OrderDTO> searchOfflineOrders(String keyword, String paymentMethod,
                                    LocalDate dateStart, LocalDate dateEnd,
                                    int page, int size);

  Page<OrderDTO> getCompletedOrders(int page, int size);

  Page<OrderDTO> searchCompletedOrders(String keyword, String paymentMethod,
                                      LocalDate dateStart, LocalDate dateEnd,
                                      int page, int size);

  Page<OrderDTO> searchCompletedOrdersWithTypeFilter(String keyword, String paymentMethod, String orderTypeFilter,
                                                     LocalDate dateStart, LocalDate dateEnd,
                                                     int page, int size);

  Order findByOrderCode(String orderCode);

  Order findByOrderCodeWithItems(String orderCode);
}
