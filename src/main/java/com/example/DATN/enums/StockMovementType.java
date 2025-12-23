package com.example.DATN.enums;

public class StockMovementType {
    public static final String IN = "IN";           // Nhập kho
    public static final String OUT = "OUT";         // Xuất kho
    public static final String SALE = "SALE";       // Bán hàng
    public static final String RETURN = "RETURN";   // Hoàn trả
    public static final String DAMAGE = "DAMAGE";   // Hàng hỏng
    public static final String MANUAL = "MANUAL";   // Cập nhật thủ công
    public static final String ADJUST = "ADJUST";   // Điều chỉnh kho
    public static final String TRANSFER = "TRANSFER"; // Chuyển kho (nếu có nhiều kho)

    private StockMovementType() {
        // Private constructor to prevent instantiation
    }
}