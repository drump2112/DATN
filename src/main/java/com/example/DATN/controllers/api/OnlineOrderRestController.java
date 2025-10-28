package com.example.DATN.controllers.api;

import com.example.DATN.models.*;
import com.example.DATN.repositories.*;
import com.example.DATN.request.OrderRequest;
import com.example.DATN.services.OrderService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OnlineOrderRestController {

    @Autowired
    private OrderService orderService;

   @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest) {
        try {
            Order order = orderService.createOrder(orderRequest);
            if ("CASH".equalsIgnoreCase(orderRequest.getPaymentMethod())) {
                return ResponseEntity.ok(Map.of(
                        "status", "WAITING_OTP",
                        "orderId", order.getId(),
                        "email", order.getUser().getEmail()
                ));
            }
            return ResponseEntity.ok(Map.of("status", "SUCCESS", "orderId", order.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }

    @PostMapping("/confirm-otp")
    public ResponseEntity<?> confirmOtp(@RequestBody Map<String, String> body) {
        try {
            Integer orderId = Integer.parseInt(body.get("orderId"));
            String email = body.get("email");
            String otp = body.get("otp");
            boolean ok = orderService.confirmOtp(orderId, email, otp);
            if (ok) return ResponseEntity.ok(Map.of("success", true));
            return ResponseEntity.ok(Map.of("success", false, "message", "OTP không hợp lệ hoặc đã hết hạn"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
