package com.example.DATN.services;

import com.example.DATN.enums.OrderStatus;
import java.util.List;

public interface OrderStatusService {
    
    /**
     * Lấy danh sách các trạng thái có thể chuyển đổi từ trạng thái hiện tại
     */
    List<OrderStatus> getValidTransitions(String currentStatus);
    
    /**
     * Kiểm tra xem việc chuyển đổi trạng thái có cần confirmation hay không
     */
    boolean requiresConfirmation(String fromStatus, String toStatus);
    
    /**
     * Lấy message confirmation cho việc chuyển đổi trạng thái
     */
    String getConfirmationMessage(String fromStatus, String toStatus);
}