package com.example.DATN.controllers.api;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.DATN.dtos.OrderDetailResponse;
import com.example.DATN.models.Order;
import com.example.DATN.request.CounterOrderRequest;
import com.example.DATN.services.OrderService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/orders")
public class OfflineOrderRestController {

  @Autowired
  private OrderService orderService;

  @PostMapping("/counter")
  public ResponseEntity<?> createCounterOrder(@RequestBody CounterOrderRequest request) {
    try {
      Order order = orderService.createCounterOrder(request);
      return ResponseEntity.ok(Map.of("success", true, "orderCode", order.getOrderCode()));
    } catch (RuntimeException ex) {
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
    }
  }

  @GetMapping("/{orderCode}/details")
  public ResponseEntity<OrderDetailResponse> getOrderDetails(@PathVariable String orderCode) {
    return ResponseEntity.ok(orderService.getOrderDetailByCode(orderCode));
  }
}
