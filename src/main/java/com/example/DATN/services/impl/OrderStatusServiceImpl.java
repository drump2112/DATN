package com.example.DATN.services.impl;

import com.example.DATN.enums.OrderStatus;
import com.example.DATN.services.OrderStatusService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class OrderStatusServiceImpl implements OrderStatusService {

    @Override
    public List<OrderStatus> getValidTransitions(String currentStatus) {
        List<OrderStatus> validTransitions = new ArrayList<>();

        try {
            OrderStatus current = OrderStatus.fromCode(currentStatus);

            // Luôn bao gồm trạng thái hiện tại
            validTransitions.add(current);

            // Thêm các trạng thái có thể chuyển đổi
            for (OrderStatus status : OrderStatus.values()) {
                if (current.canTransitionTo(status)) {
                    validTransitions.add(status);
                }
            }

        } catch (IllegalArgumentException e) {
            // Nếu trạng thái không hợp lệ, trả về tất cả
            validTransitions.addAll(Arrays.asList(OrderStatus.values()));
        }

        return validTransitions;
    }

    @Override
    public boolean requiresConfirmation(String fromStatus, String toStatus) {
        // Các trường hợp cần confirmation
        if ("PENDING".equals(fromStatus)) {
            return "SHIPPING".equals(toStatus) || "CANCELLED".equals(toStatus);
        }

        if ("SHIPPING".equals(fromStatus)) {
            return "COMPLETED".equals(toStatus);
        }

        if ("COMPLETED".equals(fromStatus)) {
            return "RETURN".equals(toStatus);
        }

        return false;
    }

    @Override
    public String getConfirmationMessage(String fromStatus, String toStatus) {
        if ("PENDING".equals(fromStatus) && "SHIPPING".equals(toStatus)) {
            return "Xác nhận đơn hàng và chuyển sang trạng thái giao hàng?";
        }

        if ("PENDING".equals(fromStatus) && "CANCELLED".equals(toStatus)) {
            return "Hủy đơn hàng này? Hành động này không thể hoàn tác.";
        }

        if ("SHIPPING".equals(fromStatus) && "COMPLETED".equals(toStatus)) {
            return "Xác nhận đơn hàng đã được giao thành công?";
        }

        if ("COMPLETED".equals(fromStatus) && "RETURN".equals(toStatus)) {
            return "Chuyển đơn hàng sang trạng thái đổi/trả?";
        }

        return "Bạn có chắc muốn thay đổi trạng thái đơn hàng?";
    }
}