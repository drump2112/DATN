package com.example.DATN.controllers.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.DATN.dtos.ShippingInfoDTO;
import com.example.DATN.services.UserShippingService;

@RestController
@RequestMapping("/api/shipping")
public class ShippingController {

    @Autowired
    private UserShippingService userShippingService;

    /**
     * Lấy thông tin giao hàng đầy đủ của người dùng
     * Bao gồm mã tỉnh, mã phường/xã để sử dụng cho API giao hàng
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ShippingInfoDTO> getUserShippingInfo(@PathVariable Integer userId) {
        try {
            ShippingInfoDTO shippingInfo = userShippingService.getUserShippingInfo(userId);
            return ResponseEntity.ok(shippingInfo);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Lấy chỉ mã tỉnh của người dùng (cho API giao hàng)
     */
    @GetMapping("/user/{userId}/province-code")
    public ResponseEntity<String> getUserProvinceCode(@PathVariable Integer userId) {
        try {
            String provinceCode = userShippingService.getUserProvinceCode(userId);
            if (provinceCode != null) {
                return ResponseEntity.ok(provinceCode);
            } else {
                return ResponseEntity.noContent().build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Lấy chỉ mã phường/xã của người dùng (cho API giao hàng)
     */
    @GetMapping("/user/{userId}/commune-code")
    public ResponseEntity<String> getUserCommuneCode(@PathVariable Integer userId) {
        try {
            String communeCode = userShippingService.getUserCommuneCode(userId);
            if (communeCode != null) {
                return ResponseEntity.ok(communeCode);
            } else {
                return ResponseEntity.noContent().build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Kiểm tra xem người dùng có địa chỉ đầy đủ để giao hàng không
     */
    @GetMapping("/user/{userId}/complete-address-check")
    public ResponseEntity<Boolean> hasCompleteAddress(@PathVariable Integer userId) {
        try {
            boolean hasComplete = userShippingService.hasCompleteAddress(userId);
            return ResponseEntity.ok(hasComplete);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Cập nhật địa chỉ cho người dùng
     */
    @PostMapping("/user/{userId}/update-address")
    public ResponseEntity<String> updateUserAddress(
            @PathVariable Integer userId,
            @RequestParam Integer addressId) {
        try {
            boolean success = userShippingService.updateUserAddress(userId, addressId);
            if (success) {
                return ResponseEntity.ok("Cập nhật địa chỉ thành công");
            } else {
                return ResponseEntity.badRequest().body("Không thể cập nhật địa chỉ");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}