package com.example.DATN.controllers.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import com.example.DATN.models.Voucher;
import com.example.DATN.repositories.VoucherRepository;
import com.example.DATN.services.VoucherService;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherRestController {

  @Autowired
  private VoucherService voucherService;

  @Autowired
  private VoucherRepository voucherRepository;

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

  /**
   * API áp dụng voucher cho bán hàng tại quầy
   * Validate và trừ số lượng ngay khi áp dụng
   */
  @PostMapping("/apply-counter")
  @Transactional
  public ResponseEntity<?> applyVoucherForCounter(@RequestBody Map<String, Object> request) {
    Integer voucherId = request.get("voucherId") != null ? Integer.parseInt(request.get("voucherId").toString()) : null;
    BigDecimal orderTotal = request.get("orderTotal") != null ? new BigDecimal(request.get("orderTotal").toString()) : BigDecimal.ZERO;

    if (voucherId == null) {
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "Voucher ID không được để trống"
      ));
    }

    Voucher voucher = voucherRepository.findById(voucherId).orElse(null);

    if (voucher == null) {
      return ResponseEntity.ok(Map.of(
          "success", false,
          "message", "Voucher không tồn tại"
      ));
    }

    // Kiểm tra số lượng
    if (voucher.getQuantity() != null && voucher.getQuantity() <= 0) {
      return ResponseEntity.ok(Map.of(
          "success", false,
          "message", "Voucher \"" + voucher.getCode() + "\" đã hết lượt sử dụng. Vui lòng chọn voucher khác."
      ));
    }

    // Kiểm tra trạng thái active
    if (voucher.getIsActive() != null && !voucher.getIsActive()) {
      return ResponseEntity.ok(Map.of(
          "success", false,
          "message", "Voucher \"" + voucher.getCode() + "\" đã bị vô hiệu hóa."
      ));
    }

    // Kiểm tra thời gian hiệu lực
    LocalDateTime now = LocalDateTime.now();
    if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
      return ResponseEntity.ok(Map.of(
          "success", false,
          "message", "Voucher \"" + voucher.getCode() + "\" chưa đến thời gian áp dụng."
      ));
    }
    if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
      return ResponseEntity.ok(Map.of(
          "success", false,
          "message", "Voucher \"" + voucher.getCode() + "\" đã hết hạn sử dụng."
      ));
    }

    // Kiểm tra giá trị đơn hàng tối thiểu
    if (voucher.getMinOrderAmount() != null && orderTotal.compareTo(voucher.getMinOrderAmount()) < 0) {
      return ResponseEntity.ok(Map.of(
          "success", false,
          "message", "Đơn hàng chưa đạt giá trị tối thiểu " + voucher.getMinOrderAmount().toPlainString() + " VNĐ để áp dụng voucher này."
      ));
    }

    // Trừ số lượng voucher
    voucher.setQuantity(voucher.getQuantity() - 1);
    voucherRepository.save(voucher);
    System.out.println("✅ Voucher applied for counter: " + voucher.getCode() + ", remaining: " + voucher.getQuantity());

    // Tính số tiền giảm
    BigDecimal discountAmount = calculateDiscount(voucher, orderTotal);

    return ResponseEntity.ok(Map.of(
        "success", true,
        "message", "Áp dụng voucher thành công",
        "voucherId", voucher.getId(),
        "voucherCode", voucher.getCode(),
        "discountAmount", discountAmount,
        "remainingQuantity", voucher.getQuantity()
    ));
  }

  /**
   * API hủy áp dụng voucher cho bán hàng tại quầy
   * Hoàn lại số lượng voucher
   */
  @PostMapping("/release-counter")
  @Transactional
  public ResponseEntity<?> releaseVoucherForCounter(@RequestBody Map<String, Object> request) {
    Integer voucherId = request.get("voucherId") != null ? Integer.parseInt(request.get("voucherId").toString()) : null;

    if (voucherId == null) {
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "Voucher ID không được để trống"
      ));
    }

    Voucher voucher = voucherRepository.findById(voucherId).orElse(null);

    if (voucher == null) {
      return ResponseEntity.ok(Map.of(
          "success", false,
          "message", "Voucher không tồn tại"
      ));
    }

    // Hoàn lại số lượng voucher
    voucher.setQuantity(voucher.getQuantity() + 1);
    voucherRepository.save(voucher);
    System.out.println("✅ Voucher released for counter: " + voucher.getCode() + ", remaining: " + voucher.getQuantity());

    return ResponseEntity.ok(Map.of(
        "success", true,
        "message", "Đã hủy áp dụng voucher",
        "remainingQuantity", voucher.getQuantity()
    ));
  }

  private BigDecimal calculateDiscount(Voucher voucher, BigDecimal orderTotal) {
    if (voucher.getDiscountType() == null || voucher.getDiscountValue() == null) {
      return BigDecimal.ZERO;
    }

    BigDecimal discount;

    if ("PERCENT".equalsIgnoreCase(voucher.getDiscountType())) {
      discount = orderTotal.multiply(voucher.getDiscountValue())
          .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
      if (voucher.getMaxDiscountValue() != null) {
        discount = discount.min(voucher.getMaxDiscountValue());
      }
    } else {
      discount = voucher.getDiscountValue();
    }

    return discount.min(orderTotal);
  }

  /**
   * API validate voucher trước khi đặt hàng
   * Kiểm tra xem voucher còn khả dụng không (còn số lượng, còn hạn, v.v.)
   */
  @GetMapping("/validate")
  public ResponseEntity<?> validateVoucher(
      @RequestParam(value = "voucherId", required = false) Integer voucherId,
      @RequestParam(value = "voucherCode", required = false) String voucherCode,
      @RequestParam(value = "orderTotal", required = false) BigDecimal orderTotal) {

    Voucher voucher = null;

    // Tìm voucher theo id hoặc code
    if (voucherId != null) {
      voucher = voucherRepository.findById(voucherId).orElse(null);
    } else if (voucherCode != null && !voucherCode.isEmpty()) {
      voucher = voucherRepository.findByCode(voucherCode);
    }

    if (voucher == null) {
      return ResponseEntity.ok(Map.of(
          "valid", false,
          "message", "Voucher không tồn tại"
      ));
    }

    // Kiểm tra số lượng
    if (voucher.getQuantity() != null && voucher.getQuantity() <= 0) {
      return ResponseEntity.ok(Map.of(
          "valid", false,
          "message", "Voucher \"" + voucher.getCode() + "\" đã hết lượt sử dụng. Vui lòng chọn voucher khác."
      ));
    }

    // Kiểm tra trạng thái active
    if (voucher.getIsActive() != null && !voucher.getIsActive()) {
      return ResponseEntity.ok(Map.of(
          "valid", false,
          "message", "Voucher \"" + voucher.getCode() + "\" đã bị vô hiệu hóa."
      ));
    }

    // Kiểm tra thời gian hiệu lực
    LocalDateTime now = LocalDateTime.now();
    if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
      return ResponseEntity.ok(Map.of(
          "valid", false,
          "message", "Voucher \"" + voucher.getCode() + "\" chưa đến thời gian áp dụng."
      ));
    }
    if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
      return ResponseEntity.ok(Map.of(
          "valid", false,
          "message", "Voucher \"" + voucher.getCode() + "\" đã hết hạn sử dụng."
      ));
    }

    // Kiểm tra giá trị đơn hàng tối thiểu
    if (orderTotal != null && voucher.getMinOrderAmount() != null) {
      if (orderTotal.compareTo(voucher.getMinOrderAmount()) < 0) {
        return ResponseEntity.ok(Map.of(
            "valid", false,
            "message", "Đơn hàng chưa đạt giá trị tối thiểu " + voucher.getMinOrderAmount().toPlainString() + " VNĐ để áp dụng voucher này."
        ));
      }
    }

    // Voucher hợp lệ
    return ResponseEntity.ok(Map.of(
        "valid", true,
        "message", "Voucher hợp lệ",
        "remainingQuantity", voucher.getQuantity() != null ? voucher.getQuantity() : -1
    ));
  }

}
