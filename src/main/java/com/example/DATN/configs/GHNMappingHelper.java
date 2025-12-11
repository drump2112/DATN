package com.example.DATN.configs;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class GHNMappingHelper {

    private static final Map<String, Integer> PROVINCE_MAPPING = new HashMap<>();

    private static final Map<String, String> WARD_MAPPING = new HashMap<>();

    static {
        PROVINCE_MAPPING.put("01", 269); // Hà Nội
        PROVINCE_MAPPING.put("79", 202); // TP. Hồ Chí Minh
        PROVINCE_MAPPING.put("31", 203); // Hải Phòng
        PROVINCE_MAPPING.put("48", 204); // Đà Nẵng
        PROVINCE_MAPPING.put("92", 205); // Cần Thơ
        PROVINCE_MAPPING.put("89", 206); // An Giang
        PROVINCE_MAPPING.put("77", 207); // Bà Rịa - Vũng Tàu
        PROVINCE_MAPPING.put("74", 208); // Bắc Giang
        PROVINCE_MAPPING.put("70", 209); // Bắc Kạn
        PROVINCE_MAPPING.put("25", 210); // Bắc Liêu
        PROVINCE_MAPPING.put("27", 211); // Bắc Ninh
        PROVINCE_MAPPING.put("83", 212); // Bến Tre
        PROVINCE_MAPPING.put("52", 213); // Bình Định
        PROVINCE_MAPPING.put("75", 214); // Bình Dương
        PROVINCE_MAPPING.put("58", 215); // Bình Phước
        PROVINCE_MAPPING.put("40", 216); // Bình Thuận
        PROVINCE_MAPPING.put("96", 217); // Cà Mau
        PROVINCE_MAPPING.put("04", 218); // Cao Bằng
        PROVINCE_MAPPING.put("66", 219); // Đắk Lắk
        PROVINCE_MAPPING.put("67", 220); // Đắk Nông
        PROVINCE_MAPPING.put("11", 221); // Điện Biên
        PROVINCE_MAPPING.put("75", 222); // Đồng Nai
        PROVINCE_MAPPING.put("87", 223); // Đồng Tháp
        PROVINCE_MAPPING.put("64", 224); // Gia Lai
        PROVINCE_MAPPING.put("02", 225); // Hà Giang
        PROVINCE_MAPPING.put("35", 226); // Hà Nam
        PROVINCE_MAPPING.put("42", 227); // Hà Tĩnh
        PROVINCE_MAPPING.put("30", 228); // Hải Dương
        PROVINCE_MAPPING.put("93", 229); // Hậu Giang
        PROVINCE_MAPPING.put("17", 230); // Hòa Bình
        PROVINCE_MAPPING.put("33", 231); // Hưng Yên
        PROVINCE_MAPPING.put("56", 232); // Khánh Hòa
        PROVINCE_MAPPING.put("91", 233); // Kiên Giang
        PROVINCE_MAPPING.put("62", 234); // Kon Tum
        PROVINCE_MAPPING.put("12", 235); // Lai Châu
        PROVINCE_MAPPING.put("68", 236); // Lâm Đồng
        PROVINCE_MAPPING.put("06", 237); // Lạng Sơn
        PROVINCE_MAPPING.put("20", 238); // Lào Cai
        PROVINCE_MAPPING.put("80", 239); // Long An
        PROVINCE_MAPPING.put("36", 240); // Nam Định
        PROVINCE_MAPPING.put("37", 241); // Nghệ An
        PROVINCE_MAPPING.put("58", 242); // Ninh Bình
        PROVINCE_MAPPING.put("26", 243); // Ninh Thuận
        PROVINCE_MAPPING.put("53", 244); // Phú Thọ
        PROVINCE_MAPPING.put("54", 245); // Phú Yên
        PROVINCE_MAPPING.put("44", 246); // Quảng Bình
        PROVINCE_MAPPING.put("49", 247); // Quảng Nam
        PROVINCE_MAPPING.put("51", 248); // Quảng Ngãi
        PROVINCE_MAPPING.put("22", 249); // Quảng Ninh
        PROVINCE_MAPPING.put("45", 250); // Quảng Trị
        PROVINCE_MAPPING.put("94", 251); // Sóc Trăng
        PROVINCE_MAPPING.put("14", 252); // Sơn La
        PROVINCE_MAPPING.put("72", 253); // Tây Ninh
        PROVINCE_MAPPING.put("34", 254); // Thái Bình
        PROVINCE_MAPPING.put("19", 255); // Thái Nguyên
        PROVINCE_MAPPING.put("38", 256); // Thanh Hóa
        PROVINCE_MAPPING.put("46", 257); // Thừa Thiên Huế
        PROVINCE_MAPPING.put("82", 258); // Tiền Giang
        PROVINCE_MAPPING.put("84", 259); // Trà Vinh
        PROVINCE_MAPPING.put("08", 260); // Tuyên Quang
        PROVINCE_MAPPING.put("86", 261); // Vĩnh Long
        PROVINCE_MAPPING.put("26", 262); // Vĩnh Phúc
        PROVINCE_MAPPING.put("15", 263); // Yên Bái
    }

    //  * Lấy GHN Province ID từ mã tỉnh hệ thống
    public Integer getGHNProvinceId(String systemProvinceCode) {
        return PROVINCE_MAPPING.getOrDefault(systemProvinceCode, 269); // Default: Hà Nội
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