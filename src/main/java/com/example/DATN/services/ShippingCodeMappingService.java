package com.example.DATN.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class ShippingCodeMappingService {

    // Mapping mã tỉnh hệ thống -> GHN Province ID
    private static final Map<String, Integer> PROVINCE_TO_GHN = new HashMap<>();

    // Mapping mã phường/xã hệ thống -> GHN District ID (ví dụ)
    private static final Map<String, Integer> COMMUNE_TO_GHN = new HashMap<>();

    static {
        // Khởi tạo mapping cho GHN - Mã tỉnh
        PROVINCE_TO_GHN.put("01", 269); // Hà Nội
        PROVINCE_TO_GHN.put("79", 202); // TP.HCM
        PROVINCE_TO_GHN.put("48", 291); // Đà Nẵng
        PROVINCE_TO_GHN.put("31", 281); // Hải Phòng
        PROVINCE_TO_GHN.put("92", 292); // Cần Thơ
        PROVINCE_TO_GHN.put("22", 203); // Quảng Ninh
        PROVINCE_TO_GHN.put("26", 268); // Vĩnh Phúc
        PROVINCE_TO_GHN.put("27", 264); // Bắc Ninh
        PROVINCE_TO_GHN.put("30", 265); // Hải Dương
        PROVINCE_TO_GHN.put("75", 260); // Đồng Nai
        PROVINCE_TO_GHN.put("74", 261); // Bình Dương

        // Khởi tạo mapping cho GHN - Mã phường/xã (ví dụ một số phường Hà Nội)
        COMMUNE_TO_GHN.put("00001", 1442); // Phúc Xá, Ba Đình
        COMMUNE_TO_GHN.put("00004", 1443); // Trúc Bạch, Ba Đình
        COMMUNE_TO_GHN.put("00037", 1444); // Phúc Tân, Hoàn Kiếm
        COMMUNE_TO_GHN.put("00040", 1445); // Đồng Xuân, Hoàn Kiếm

        // Ví dụ một số phường TP.HCM
        COMMUNE_TO_GHN.put("26734", 1460); // Tân Định, Quận 1
        COMMUNE_TO_GHN.put("26737", 1461); // Đa Kao, Quận 1
        COMMUNE_TO_GHN.put("26740", 1462); // Bến Nghé, Quận 1
    }

    /**
     * Chuyển đổi mã tỉnh hệ thống sang GHN Province ID
     */
    public Integer convertProvinceCodeToGHN(String provinceCode) {
        return PROVINCE_TO_GHN.get(provinceCode);
    }

    /**
     * Chuyển đổi mã phường/xã hệ thống sang GHN District ID
     */
    public Integer convertCommuneCodeToGHN(String communeCode) {
        return COMMUNE_TO_GHN.get(communeCode);
    }

    /**
     * Chuyển đổi mã tỉnh hệ thống sang VTP Province Code
     * VTP thường sử dụng tên tỉnh hoặc mã khác
     */
    public String convertProvinceCodeToVTP(String provinceCode) {
        Map<String, String> vtpMapping = new HashMap<>();
        vtpMapping.put("01", "HN"); // Hà Nội
        vtpMapping.put("79", "HCM"); // TP.HCM
        vtpMapping.put("48", "DN"); // Đà Nẵng
        vtpMapping.put("31", "HP"); // Hải Phòng
        vtpMapping.put("92", "CT"); // Cần Thơ

        return vtpMapping.get(provinceCode);
    }

    /**
     * Chuyển đổi mã phường/xã sang VTP District Code
     */
    public String convertCommuneCodeToVTP(String communeCode) {
        // VTP có thể sử dụng mã khác, cần mapping riêng
        Map<String, String> vtpCommuneMapping = new HashMap<>();
        vtpCommuneMapping.put("00001", "BA_DINH_PHUC_XA");
        vtpCommuneMapping.put("26734", "QUAN_1_TAN_DINH");

        return vtpCommuneMapping.get(communeCode);
    }

    /**
     * Chuyển đổi mã tỉnh sang J&T Express
     */
    public String convertProvinceCodeToJT(String provinceCode) {
        Map<String, String> jtMapping = new HashMap<>();
        jtMapping.put("01", "HANOI");
        jtMapping.put("79", "HOCHIMINH");
        jtMapping.put("48", "DANANG");

        return jtMapping.get(provinceCode);
    }

    /**
     * Kiểm tra xem có hỗ trợ giao hàng đến tỉnh này với GHN không
     */
    public boolean isGHNSupportedProvince(String provinceCode) {
        return PROVINCE_TO_GHN.containsKey(provinceCode);
    }

    /**
     * Kiểm tra xem có hỗ trợ giao hàng đến phường/xã này với GHN không
     */
    public boolean isGHNSupportedCommune(String communeCode) {
        return COMMUNE_TO_GHN.containsKey(communeCode);
    }

    /**
     * Lấy danh sách tất cả tỉnh được hỗ trợ bởi GHN
     */
    public Map<String, Integer> getAllGHNSupportedProvinces() {
        return new HashMap<>(PROVINCE_TO_GHN);
    }
}