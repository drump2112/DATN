package com.example.DATN.controllers.api;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.DATN.dtos.ShippingInfoDTO;
import com.example.DATN.services.ShippingCodeMappingService;
import com.example.DATN.services.UserShippingService;

@RestController
@RequestMapping("/api/shipping-integration")
public class ShippingIntegrationController {

    @Autowired
    private UserShippingService userShippingService;

    @Autowired
    private ShippingCodeMappingService mappingService;

    /**
     * Lấy thông tin để tích hợp với GHN API
     */
    @GetMapping("/ghn/user/{userId}")
    public ResponseEntity<Map<String, Object>> getGHNShippingData(@PathVariable Integer userId) {
        try {
            ShippingInfoDTO shippingInfo = userShippingService.getUserShippingInfo(userId);

            if (!userShippingService.hasCompleteAddress(userId)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Địa chỉ không đầy đủ"));
            }

            Integer ghnProvinceId = mappingService.convertProvinceCodeToGHN(shippingInfo.getProvinceCode());
            Integer ghnDistrictId = mappingService.convertCommuneCodeToGHN(shippingInfo.getCommuneCode());

            if (ghnProvinceId == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Tỉnh không được GHN hỗ trợ"));
            }

            Map<String, Object> ghnData = Map.of(
                "to_name", shippingInfo.getUserFullName(),
                "to_phone", shippingInfo.getUserPhone(),
                "to_address", shippingInfo.getSpecificAddress(),
                "to_province_id", ghnProvinceId,
                "to_district_id", ghnDistrictId != null ? ghnDistrictId : 0,
                "province_code", shippingInfo.getProvinceCode(),
                "commune_code", shippingInfo.getCommuneCode(),
                "full_address", shippingInfo.getFullAddress()
            );

            return ResponseEntity.ok(ghnData);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Lấy thông tin để tích hợp với VTP API
     */
    @GetMapping("/vtp/user/{userId}")
    public ResponseEntity<Map<String, Object>> getVTPShippingData(@PathVariable Integer userId) {
        try {
            ShippingInfoDTO shippingInfo = userShippingService.getUserShippingInfo(userId);

            if (!userShippingService.hasCompleteAddress(userId)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Địa chỉ không đầy đủ"));
            }

            String vtpProvinceCode = mappingService.convertProvinceCodeToVTP(shippingInfo.getProvinceCode());
            String vtpDistrictCode = mappingService.convertCommuneCodeToVTP(shippingInfo.getCommuneCode());

            Map<String, Object> vtpData = Map.of(
                "RECEIVER_FULLNAME", shippingInfo.getUserFullName(),
                "RECEIVER_PHONE", shippingInfo.getUserPhone(),
                "RECEIVER_ADDRESS", shippingInfo.getSpecificAddress(),
                "RECEIVER_PROVINCE", vtpProvinceCode != null ? vtpProvinceCode : shippingInfo.getProvinceCode(),
                "RECEIVER_DISTRICT", vtpDistrictCode != null ? vtpDistrictCode : shippingInfo.getCommuneCode(),
                "province_code", shippingInfo.getProvinceCode(),
                "commune_code", shippingInfo.getCommuneCode(),
                "full_address", shippingInfo.getFullAddress()
            );

            return ResponseEntity.ok(vtpData);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Lấy thông tin để tích hợp với J&T Express API
     */
    @GetMapping("/jt/user/{userId}")
    public ResponseEntity<Map<String, Object>> getJTShippingData(@PathVariable Integer userId) {
        try {
            ShippingInfoDTO shippingInfo = userShippingService.getUserShippingInfo(userId);

            if (!userShippingService.hasCompleteAddress(userId)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Địa chỉ không đầy đủ"));
            }

            String jtProvinceCode = mappingService.convertProvinceCodeToJT(shippingInfo.getProvinceCode());

            Map<String, Object> jtData = Map.of(
                "receiver_name", shippingInfo.getUserFullName(),
                "receiver_phone", shippingInfo.getUserPhone(),
                "receiver_address", shippingInfo.getSpecificAddress(),
                "receiver_city", jtProvinceCode != null ? jtProvinceCode : shippingInfo.getProvinceName(),
                "receiver_area", shippingInfo.getCommuneName(),
                "province_code", shippingInfo.getProvinceCode(),
                "commune_code", shippingInfo.getCommuneCode(),
                "full_address", shippingInfo.getFullAddress()
            );

            return ResponseEntity.ok(jtData);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Kiểm tra hỗ trợ giao hàng của các nhà vận chuyển
     */
    @GetMapping("/support-check/user/{userId}")
    public ResponseEntity<Map<String, Object>> checkShippingSupport(@PathVariable Integer userId) {
        try {
            ShippingInfoDTO shippingInfo = userShippingService.getUserShippingInfo(userId);

            if (shippingInfo.getProvinceCode() == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Không có thông tin tỉnh"));
            }

            boolean ghnProvinceSupport = mappingService.isGHNSupportedProvince(shippingInfo.getProvinceCode());
            boolean ghnCommuneSupport = mappingService.isGHNSupportedCommune(shippingInfo.getCommuneCode());

            Map<String, Object> supportInfo = Map.of(
                "province_code", shippingInfo.getProvinceCode(),
                "province_name", shippingInfo.getProvinceName(),
                "commune_code", shippingInfo.getCommuneCode(),
                "commune_name", shippingInfo.getCommuneName(),
                "ghn_province_support", ghnProvinceSupport,
                "ghn_commune_support", ghnCommuneSupport,
                "ghn_full_support", ghnProvinceSupport && ghnCommuneSupport,
                "has_complete_address", userShippingService.hasCompleteAddress(userId)
            );

            return ResponseEntity.ok(supportInfo);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Tính phí giao hàng (demo - cần tích hợp với API thực)
     */
    @PostMapping("/calculate-fee/user/{userId}")
    public ResponseEntity<Map<String, Object>> calculateShippingFee(
            @PathVariable Integer userId,
            @RequestParam String carrier, // ghn, vtp, jt
            @RequestParam(defaultValue = "1000") Integer weight,
            @RequestParam(defaultValue = "1") Integer quantity) {

        try {
            ShippingInfoDTO shippingInfo = userShippingService.getUserShippingInfo(userId);

            if (!userShippingService.hasCompleteAddress(userId)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Địa chỉ không đầy đủ"));
            }

            // Demo calculation - trong thực tế sẽ gọi API của nhà vận chuyển
            Map<String, Object> feeInfo = Map.of(
                "carrier", carrier,
                "province_code", shippingInfo.getProvinceCode(),
                "commune_code", shippingInfo.getCommuneCode(),
                "weight", weight,
                "quantity", quantity,
                "estimated_fee", calculateDemoFee(carrier, shippingInfo.getProvinceCode(), weight),
                "delivery_time", estimateDeliveryTime(carrier, shippingInfo.getProvinceCode())
            );

            return ResponseEntity.ok(feeInfo);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Demo methods
    private Integer calculateDemoFee(String carrier, String provinceCode, Integer weight) {
        int baseFee = 30000; // 30k VND

        // Tỉnh xa hơn phí cao hơn
        if (!provinceCode.equals("01") && !provinceCode.equals("79")) {
            baseFee += 20000;
        }

        // Trọng lượng
        if (weight > 1000) {
            baseFee += ((weight - 1000) / 500) * 5000;
        }

        return baseFee;
    }

    private String estimateDeliveryTime(String carrier, String provinceCode) {
        if (provinceCode.equals("01") || provinceCode.equals("79")) {
            return "1-2 ngày";
        }
        return "2-3 ngày";
    }
}