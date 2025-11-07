package com.example.DATN.enums;

public enum OrderStatus {
    PENDING("PENDING", "Chờ xử lý"),
    PROCESSING("PROCESSING", "Đang xử lý"),
    SHIPPING("SHIPPING", "Đang giao hàng"),
    COMPLETED("COMPLETED", "Hoàn thành"),
    CANCELLED("CANCELLED", "Đã hủy"),
    RETURN("RETURN", "Đổi/Trả"),
    WAITING_OTP("WAITING_OTP", "Chờ xác nhận OTP");

    private final String code;
    private final String displayName;

    OrderStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static OrderStatus fromCode(String code) {
        for (OrderStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid order status code: " + code);
    }

    /**
     * Kiểm tra trạng thái có thể chuyển đổi từ trạng thái hiện tại hay không
     */
    public boolean canTransitionTo(OrderStatus newStatus) {
        switch (this) {
            case WAITING_OTP:
                return newStatus == PENDING || newStatus == CANCELLED;

            case PENDING:
                return newStatus == PROCESSING || newStatus == SHIPPING || newStatus == CANCELLED;

            case PROCESSING:
                return newStatus == SHIPPING || newStatus == COMPLETED || newStatus == CANCELLED;

            case SHIPPING:
                return newStatus == COMPLETED || newStatus == RETURN;

            case COMPLETED:
                return newStatus == RETURN;

            case CANCELLED:
            case RETURN:
                return false; // Trạng thái cuối, không thể chuyển đổi

            default:
                return false;
        }
    }
}