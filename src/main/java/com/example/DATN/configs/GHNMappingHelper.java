package com.example.DATN.configs;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class GHNMappingHelper {

    private static final Map<String, Integer> PROVINCE_MAPPING = new HashMap<>();

    private static final Map<String, String> WARD_MAPPING = new HashMap<>();

    static {
        // 34 tỉnh thành trong hệ thống - Mapping từ GHN API (cập nhật 2025-12-03)
        // Danh sách chính xác 34 tỉnh thành hiện có ở Việt Nam

        // Thành phố trực thuộc Trung ương
        PROVINCE_MAPPING.put("01", 201);  // Hà Nội
        PROVINCE_MAPPING.put("79", 202);  // Hồ Chí Minh
        PROVINCE_MAPPING.put("31", 224);  // Hải Phòng
        PROVINCE_MAPPING.put("48", 203);  // Đà Nẵng
        PROVINCE_MAPPING.put("92", 220);  // Cần Thơ

        // Tỉnh miền Bắc
        PROVINCE_MAPPING.put("04", 246);  // Cao Bằng
        PROVINCE_MAPPING.put("08", 228);  // Tuyên Quang
        PROVINCE_MAPPING.put("11", 265);  // Điện Biên
        PROVINCE_MAPPING.put("12", 264);  // Lai Châu
        PROVINCE_MAPPING.put("14", 266);  // Sơn La
        PROVINCE_MAPPING.put("15", 269);  // Lào Cai
        PROVINCE_MAPPING.put("19", 244);  // Thái Nguyên
        PROVINCE_MAPPING.put("20", 247);  // Lạng Sơn
        PROVINCE_MAPPING.put("22", 230);  // Quảng Ninh
        PROVINCE_MAPPING.put("24", 249);  // Bắc Ninh
        PROVINCE_MAPPING.put("25", 229);  // Phú Thọ
        PROVINCE_MAPPING.put("33", 268);  // Hưng Yên
        PROVINCE_MAPPING.put("37", 233);  // Ninh Bình

        // Tỉnh miền Trung
        PROVINCE_MAPPING.put("38", 234);  // Thanh Hóa
        PROVINCE_MAPPING.put("40", 235);  // Nghệ An
        PROVINCE_MAPPING.put("42", 236);  // Hà Tĩnh
        PROVINCE_MAPPING.put("44", 238);  // Quảng Trị
        PROVINCE_MAPPING.put("46", 223);  // Huế
        PROVINCE_MAPPING.put("51", 242);  // Quảng Ngãi
        PROVINCE_MAPPING.put("52", 207);  // Gia Lai
        PROVINCE_MAPPING.put("56", 208);  // Khánh Hòa
        PROVINCE_MAPPING.put("66", 210);  // Đắk Lắk

        // Tỉnh miền Nam
        PROVINCE_MAPPING.put("68", 209);  // Lâm Đồng
        PROVINCE_MAPPING.put("75", 204);  // Đồng Nai
        PROVINCE_MAPPING.put("80", 240);  // Tây Ninh
        PROVINCE_MAPPING.put("82", 216);  // Đồng Tháp
        PROVINCE_MAPPING.put("86", 215);  // Vĩnh Long
        PROVINCE_MAPPING.put("91", 217);  // An Giang
        PROVINCE_MAPPING.put("96", 252);  // Cà Mau
    }

    //  * Lấy GHN Province ID từ mã tỉnh hệ thống
    public Integer getGHNProvinceId(String systemProvinceCode) {
        return PROVINCE_MAPPING.getOrDefault(systemProvinceCode, 201); // Default: Hà Nội
    }

    //   Lấy GHN Ward Code từ mã phường hệ thống
    //   (Cần được cập nhật với data thực tế)
    public String getGHNWardCode(String systemCommuneCode) {
        // Tạm thời return null để GHN service tự tìm ward đầu tiên
        return WARD_MAPPING.get(systemCommuneCode);
    }

    // Kiểm tra xem có hỗ trợ tỉnh này không
    public boolean isProvinceSupported(String systemProvinceCode) {
        return PROVINCE_MAPPING.containsKey(systemProvinceCode);
    }
}