package com.example.DATN.controllers.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.DATN.services.OfflineCartHoldService;

/**
 * Controller để quản lý việc giữ/hoàn lại số lượng sản phẩm
 * khi thêm/xóa sản phẩm vào giỏ hàng offline (bán tại quầy)
 */
@RestController
@RequestMapping("/api/offline-cart")
public class OfflineCartHoldController {

    @Autowired
    private OfflineCartHoldService offlineCartHoldService;

    /**
     * Giữ số lượng sản phẩm (trừ trong DB) khi thêm vào giỏ hàng
     */
    @PostMapping("/hold")
    public ResponseEntity<?> holdStock(@RequestBody HoldRequest request) {
        try {
            boolean success = offlineCartHoldService.holdStock(
                    request.getVariantId(),
                    request.getQuantity(),
                    request.getSessionId()
            );

            if (success) {
                int remainingStock = offlineCartHoldService.getAvailableStock(request.getVariantId());
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "remainingStock", remainingStock
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Không đủ số lượng tồn kho"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Lỗi hệ thống: " + e.getMessage()
            ));
        }
    }

    /**
     * Hoàn lại số lượng sản phẩm khi xóa khỏi giỏ hàng
     */
    @PostMapping("/release")
    public ResponseEntity<?> releaseStock(@RequestBody ReleaseRequest request) {
        try {
            boolean success = offlineCartHoldService.releaseStock(
                    request.getVariantId(),
                    request.getQuantity(),
                    request.getSessionId()
            );

            if (success) {
                int remainingStock = offlineCartHoldService.getAvailableStock(request.getVariantId());
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "remainingStock", remainingStock
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Không thể hoàn lại số lượng"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Lỗi hệ thống: " + e.getMessage()
            ));
        }
    }

    /**
     * Hoàn lại tất cả số lượng sản phẩm khi reload hoặc đóng trang
     */
    @PostMapping("/release-all")
    public ResponseEntity<?> releaseAllStock(@RequestBody ReleaseAllRequest request) {
        try {
            Map<Integer, Integer> heldItems = new HashMap<>();
            if (request.getItems() != null) {
                for (HeldItem item : request.getItems()) {
                    heldItems.merge(item.getVariantId(), item.getQuantity(), Integer::sum);
                }
            }

            boolean success = offlineCartHoldService.releaseAllStock(
                    request.getSessionId(),
                    heldItems
            );

            return ResponseEntity.ok(Map.of("success", success));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Lỗi hệ thống: " + e.getMessage()
            ));
        }
    }

    /**
     * Cập nhật số lượng giữ khi thay đổi số lượng trong giỏ hàng
     */
    @PostMapping("/update")
    public ResponseEntity<?> updateHoldStock(@RequestBody UpdateHoldRequest request) {
        try {
            boolean success = offlineCartHoldService.updateHoldStock(
                    request.getVariantId(),
                    request.getOldQuantity(),
                    request.getNewQuantity(),
                    request.getSessionId()
            );

            if (success) {
                int remainingStock = offlineCartHoldService.getAvailableStock(request.getVariantId());
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "remainingStock", remainingStock
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Không đủ số lượng tồn kho"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Lỗi hệ thống: " + e.getMessage()
            ));
        }
    }

    // Request DTOs
    public static class HoldRequest {
        private Integer variantId;
        private int quantity;
        private String sessionId;

        public Integer getVariantId() { return variantId; }
        public void setVariantId(Integer variantId) { this.variantId = variantId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    }

    public static class ReleaseRequest {
        private Integer variantId;
        private int quantity;
        private String sessionId;

        public Integer getVariantId() { return variantId; }
        public void setVariantId(Integer variantId) { this.variantId = variantId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    }

    public static class ReleaseAllRequest {
        private String sessionId;
        private List<HeldItem> items;

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public List<HeldItem> getItems() { return items; }
        public void setItems(List<HeldItem> items) { this.items = items; }
    }

    public static class HeldItem {
        private Integer variantId;
        private int quantity;

        public Integer getVariantId() { return variantId; }
        public void setVariantId(Integer variantId) { this.variantId = variantId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    public static class UpdateHoldRequest {
        private Integer variantId;
        private int oldQuantity;
        private int newQuantity;
        private String sessionId;

        public Integer getVariantId() { return variantId; }
        public void setVariantId(Integer variantId) { this.variantId = variantId; }
        public int getOldQuantity() { return oldQuantity; }
        public void setOldQuantity(int oldQuantity) { this.oldQuantity = oldQuantity; }
        public int getNewQuantity() { return newQuantity; }
        public void setNewQuantity(int newQuantity) { this.newQuantity = newQuantity; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    }
}
