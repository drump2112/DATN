package com.example.DATN.controllers.api;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.DATN.services.VoucherService;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherRestController {

  @Autowired
  private VoucherService voucherService;

  @GetMapping("/suggest")
  public ResponseEntity<?> suggestVoucher(@RequestParam("orderTotal") BigDecimal orderTotal) {
    return voucherService.suggestBestVoucher(orderTotal)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.ok().body(null));
  }

  @GetMapping("/available")
  public ResponseEntity<?> availableVouchers(@RequestParam(value = "orderTotal", required = false) BigDecimal orderTotal) {
    if (orderTotal == null) orderTotal = BigDecimal.ZERO;
    return ResponseEntity.ok(voucherService.getAvailableVouchersWithComputed(orderTotal));
  }
}
