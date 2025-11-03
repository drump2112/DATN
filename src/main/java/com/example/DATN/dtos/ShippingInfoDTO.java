package com.example.DATN.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingInfoDTO {
    private Integer userId;
    private String userFullName;
    private String userPhone;
    
    // Thông tin địa chỉ
    private Integer addressId;
    private String specificAddress; // Số nhà, tên đường
    private String fullAddress; // Địa chỉ đầy đủ
    
    // Mã code cho API giao hàng
    private String provinceCode;    // Mã tỉnh (VD: "01", "79")
    private String provinceName;    // Tên tỉnh (VD: "Hà Nội", "TP Hồ Chí Minh")
    private String communeCode;     // Mã phường/xã (VD: "00001", "26734")
    private String communeName;     // Tên phường/xã (VD: "Phúc Xá", "Tân Định")
    
    // Thông tin bổ sung cho API giao hàng
    private String provinceFullName; // VD: "Thành phố Hà Nội"
    private String communeFullName;  // VD: "Phường Phúc Xá"
}
