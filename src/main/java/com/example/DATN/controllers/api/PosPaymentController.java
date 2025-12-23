package com.example.DATN.controllers.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/pos/payment")
public class PosPaymentController {

    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestBody Map<String, Object> req) {
        double amount = Double.parseDouble(req.get("amount").toString());
        String orderCode = "POS" + System.currentTimeMillis();

        String qrUrl = "https://img.vietqr.io/image/MB-9704223451-compact.png?amount="
                + (long) amount +
                "&addInfo=ThanhToan-" + orderCode;

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("orderCode", orderCode);
        res.put("qrUrl", qrUrl);
        res.put("message", "Tạo QR thành công");
        return ResponseEntity.ok(res);
    }

    @PostMapping("/callback")
    public ResponseEntity<?> confirmPayment(@RequestBody Map<String, Object> body) {
        String orderCode = (String) body.get("orderCode");

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("orderCode", orderCode);
        res.put("message", "Thanh toán thành công (giả lập)");
        return ResponseEntity.ok(res);
    }
  }