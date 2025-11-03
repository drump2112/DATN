package com.example.DATN.controllers.api;

import com.example.DATN.dtos.OrderDetailResponse;
import com.example.DATN.models.*;
import com.example.DATN.repositories.*;
import com.example.DATN.request.OrderRequest;
import com.example.DATN.services.OrderService;

import jakarta.servlet.http.HttpServletRequest;

import com.example.DATN.configs.vnpay.VNPayService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OnlineOrderRestController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private VNPayService vnPayService;

   @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest, HttpServletRequest request) {
        try {
            Order order = orderService.createOrder(orderRequest);

            if ("CASH".equalsIgnoreCase(orderRequest.getPaymentMethod())) {
                return ResponseEntity.ok(Map.of(
                        "status", "WAITING_OTP",
                        "orderId", order.getId(),
                        "email", order.getUser().getEmail()
                ));
            } else if ("VNPAY".equalsIgnoreCase(orderRequest.getPaymentMethod())) {
                try {
                    // Validate order before creating VNPay URL
                    System.out.println("=== ORDER VALIDATION BEFORE VNPAY ===");
                    System.out.println("Order ID: " + order.getId());
                    System.out.println("Order Code: " + order.getOrderCode());
                    System.out.println("Order Status: " + order.getStatus());
                    System.out.println("Total Amount: " + order.getTotalAmount());
                    System.out.println("Payment Method: " + order.getPaymentMethod());
                    System.out.println("User ID: " + order.getUser().getId());
                    System.out.println("Items count: " + (order.getItems() != null ? order.getItems().size() : 0));

                    if (order.getId() == null) {
                        throw new RuntimeException("Order ID is null - order not saved properly");
                    }
                    if (order.getTotalAmount() == null || order.getTotalAmount().longValue() <= 0) {
                        throw new RuntimeException("Invalid order total amount: " + order.getTotalAmount());
                    }

                    String vnpayUrl = vnPayService.createPaymentUrl(order, request);
                    if (vnpayUrl != null && !vnpayUrl.isEmpty()) {
                        System.out.println("✅ VNPay URL created successfully: " + vnpayUrl.substring(0, Math.min(100, vnpayUrl.length())) + "...");
                        return ResponseEntity.ok(Map.of(
                                "status", "VNPAY_REDIRECT",
                                "paymentUrl", vnpayUrl,
                                "orderId", order.getId()
                        ));
                    } else {
                        throw new RuntimeException("Không thể tạo liên kết thanh toán VNPay");
                    }
                } catch (Exception vnpayException) {
                    // Log lỗi chi tiết
                    System.err.println("VNPay Error: " + vnpayException.getMessage());
                    vnpayException.printStackTrace();

                    return ResponseEntity.badRequest().body(Map.of(
                            "status", "ERROR",
                            "message", "Lỗi khi tạo thanh toán VNPay: " + vnpayException.getMessage()
                    ));
                }
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

    @GetMapping("/detail/{orderCode}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@PathVariable String orderCode) {
        try {
            System.out.println("=== Fetching order detail for code: " + orderCode);
            OrderDetailResponse orderDetail = orderService.getOrderDetailByCode(orderCode);

            if (orderDetail == null) {
                System.out.println("Order not found: " + orderCode);
                return ResponseEntity.notFound().build();
            }

            System.out.println("Order found: " + orderDetail.getOrderCode());
            System.out.println("Items count: " + (orderDetail.getItems() != null ? orderDetail.getItems().size() : 0));

            return ResponseEntity.ok(orderDetail);
        } catch (Exception e) {
            System.err.println("Error fetching order detail: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
