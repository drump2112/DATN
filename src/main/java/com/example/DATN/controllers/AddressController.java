package com.example.DATN.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.DATN.dtos.SimpleDTO;
import com.example.DATN.models.Address;
import com.example.DATN.models.Commune;
import com.example.DATN.models.Province;
import com.example.DATN.services.AddressService;

@RestController
@RequestMapping("/api")
public class AddressController {

    @Autowired
    private AddressService addressService;

    // Province endpoints
    @GetMapping("/provinces")
    public ResponseEntity<List<SimpleDTO>> getAllProvinces() {
        List<Province> provinces = addressService.getAllProvinces();
        List<SimpleDTO> provinceDTOs = provinces.stream()
                .map(p -> new SimpleDTO(p.getProvinceCode(), p.getProvinceName()))
                .toList();
        return ResponseEntity.ok(provinceDTOs);
    }

    @GetMapping("/provinces/{provinceCode}")
    public ResponseEntity<Province> getProvinceByCode(@PathVariable String provinceCode) {
        Optional<Province> province = addressService.getProvinceByCode(provinceCode);
        return province.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/provinces/search")
    public ResponseEntity<List<Province>> searchProvinces(@RequestParam String name) {
        List<Province> provinces = addressService.searchProvincesByName(name);
        return ResponseEntity.ok(provinces);
    }

    // Commune endpoints (thay thế cho districts và wards)
    @GetMapping("/communes")
    public ResponseEntity<List<SimpleDTO>> getCommunesByProvince(@RequestParam String provinceCode) {
        List<Commune> communes = addressService.getCommunesByProvinceCode(provinceCode);
        List<SimpleDTO> communeDTOs = communes.stream()
                .map(c -> new SimpleDTO(c.getCommuneCode(), c.getCommuneName()))
                .toList();
        return ResponseEntity.ok(communeDTOs);
    }

    @GetMapping("/communes/{communeCode}")
    public ResponseEntity<Commune> getCommuneByCode(@PathVariable String communeCode) {
        Optional<Commune> commune = addressService.getCommuneByCode(communeCode);
        return commune.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/communes/search")
    public ResponseEntity<List<Commune>> searchCommunes(@RequestParam String name) {
        List<Commune> communes = addressService.searchCommunesByName(name);
        return ResponseEntity.ok(communes);
    }

    // Address endpoints
    @PostMapping("/addresses")
    public ResponseEntity<Address> createAddress(
            @RequestParam String specificAddress,
            @RequestParam String communeCode,
            @RequestParam String provinceCode) {
        try {
            Address address = addressService.createAddress(specificAddress, communeCode, provinceCode);
            return ResponseEntity.ok(address);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<Address> updateAddress(
            @PathVariable Integer addressId,
            @RequestParam(required = false) String specificAddress,
            @RequestParam(required = false) String communeCode,
            @RequestParam(required = false) String provinceCode) {
        try {
            Address address = addressService.updateAddress(addressId, specificAddress, communeCode, provinceCode);
            return ResponseEntity.ok(address);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/addresses/{addressId}")
    public ResponseEntity<Address> getAddressById(@PathVariable Integer addressId) {
        Optional<Address> address = addressService.getAddressById(addressId);
        return address.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<Address>> getAllAddresses() {
        List<Address> addresses = addressService.getAllActiveAddresses();
        return ResponseEntity.ok(addresses);
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Integer addressId) {
        addressService.deleteAddress(addressId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/addresses/search")
    public ResponseEntity<List<Address>> searchAddresses(@RequestParam String keyword) {
        List<Address> addresses = addressService.searchAddressesByKeyword(keyword);
        return ResponseEntity.ok(addresses);
    }

    // Test GHN API connection
    @GetMapping("/test-ghn")
    public ResponseEntity<?> testGHNConnection() {
        try {
            // Test với Hà Nội -> TP.HCM
            double fee = addressService.calculateShippingFee("01", "01001", 500, 100000.0);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "GHN API hoạt động bình thường");
            result.put("testFee", fee);
            result.put("testRoute", "Hà Nội -> TP.HCM, 500g, 100,000 VNĐ");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi kết nối GHN API: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // API tính phí giao hàng
    @GetMapping("/shipping-fee")
    public ResponseEntity<?> calculateShippingFee(
            @RequestParam String provinceCode,
            @RequestParam(required = false) String communeCode,
            @RequestParam(required = false) Integer weight,
            @RequestParam(required = false) Double totalValue) {

        try {
            double shippingFee = addressService.calculateShippingFee(provinceCode, communeCode, weight, totalValue);
            return ResponseEntity.ok(new ShippingFeeResponse(shippingFee));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Lỗi tính phí giao hàng: " + e.getMessage()));
        }
    }    // DTO classes for responses
    public static class ShippingFeeResponse {
        private double fee;
        private String message;

        public ShippingFeeResponse(double fee) {
            this.fee = fee;
            this.message = "Tính phí thành công";
        }

        // Getters and setters
        public double getFee() { return fee; }
        public void setFee(double fee) { this.fee = fee; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}
