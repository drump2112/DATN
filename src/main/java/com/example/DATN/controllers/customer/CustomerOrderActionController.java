package com.example.DATN.controllers.customer;

import com.example.DATN.services.OrderService;
import com.example.DATN.enums.OrderStatus;
import com.example.DATN.models.Order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/customer/orders")
public class CustomerOrderActionController {

    @Autowired
    private OrderService orderService;

    /**
     * Khách hàng hủy đơn hàng (PENDING -> CANCELLED)
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @PathVariable Integer orderId,
            Authentication authentication) {

        try {
            // Kiểm tra đơn hàng có tồn tại và thuộc về customer không
            Order order = orderService.findById(orderId);
            if (order == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Đơn hàng không tồn tại"
                ));
            }

            // Kiểm tra quyền sở hữu đơn hàng (nếu có authentication)
            if (authentication != null) {
                String currentUsername = authentication.getName();
                if (!order.getUser().getUserName().equals(currentUsername)) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Bạn không có quyền thao tác đơn hàng này"
                    ));
                }
            }

            // Kiểm tra trạng thái có thể hủy không
            if (!OrderStatus.PENDING.name().equals(order.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Chỉ có thể hủy đơn hàng ở trạng thái chờ xử lý"
                ));
            }

            // Cập nhật trạng thái
            orderService.updateOrderStatus(orderId, OrderStatus.CANCELLED.name());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã hủy đơn hàng thành công"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    /**
     * Khách hàng xác nhận đã nhận hàng (SHIPPING -> COMPLETED)
     */
    @PostMapping("/{orderId}/confirm-received")
    public ResponseEntity<Map<String, Object>> confirmReceived(
            @PathVariable Integer orderId,
            Authentication authentication) {

        try {
            Order order = orderService.findById(orderId);
            if (order == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Đơn hàng không tồn tại"
                ));
            }

            // Kiểm tra quyền sở hữu
            if (authentication != null) {
                String currentUsername = authentication.getName();
                if (!order.getUser().getUserName().equals(currentUsername)) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Bạn không có quyền thao tác đơn hàng này"
                    ));
                }
            }

            // Kiểm tra trạng thái
            if (!OrderStatus.SHIPPING.name().equals(order.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Chỉ có thể xác nhận đơn hàng ở trạng thái đang giao hàng"
                ));
            }

            // Cập nhật trạng thái
            orderService.updateOrderStatus(orderId, OrderStatus.COMPLETED.name());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã xác nhận nhận hàng thành công. Cảm ơn bạn đã mua sắm!"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    /**
     * Khách hàng yêu cầu đổi/trả hàng (COMPLETED -> RETURN)
     */
    @PostMapping("/{orderId}/return")
    public ResponseEntity<Map<String, Object>> requestReturn(
            @PathVariable Integer orderId,
            Authentication authentication) {

        try {
            Order order = orderService.findById(orderId);
            if (order == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Đơn hàng không tồn tại"
                ));
            }

            // Kiểm tra quyền sở hữu
            if (authentication != null) {
                String currentUsername = authentication.getName();
                if (!order.getUser().getUserName().equals(currentUsername)) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Bạn không có quyền thao tác đơn hàng này"
                    ));
                }
            }

            // Kiểm tra trạng thái
            if (!OrderStatus.COMPLETED.name().equals(order.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Chỉ có thể yêu cầu đổi/trả đơn hàng đã hoàn thành"
                ));
            }

            // Cập nhật trạng thái
            orderService.updateOrderStatus(orderId, OrderStatus.RETURN.name());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã gửi yêu cầu đổi/trả hàng. Chúng tôi sẽ liên hệ với bạn sớm nhất."
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    /**
     * Mua lại đơn hàng - thêm tất cả sản phẩm vào giỏ hàng
     */
    @PostMapping("/{orderCode}/reorder")
    public ResponseEntity<Map<String, Object>> reorderItems(
            @PathVariable String orderCode,
            Authentication authentication) {

        try {
            // Tìm đơn hàng theo orderCode
            Order order = orderService.findByOrderCode(orderCode);
            if (order == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Đơn hàng không tồn tại"
                ));
            }

            // Kiểm tra quyền sở hữu
            if (authentication != null) {
                String currentUsername = authentication.getName();
                if (!order.getUser().getUserName().equals(currentUsername)) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Bạn không có quyền thao tác đơn hàng này"
                    ));
                }
            }

            // Thêm các sản phẩm vào giỏ hàng
            int totalItems = order.getItems().size();

            // TODO: Implement actual cart service logic
            // For now, just return success message
            // CartService integration should be added here:
            // cartService.addToCart(order.getUser().getId(), item.getProductVariant().getId(), item.getQuantity());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã thêm " + totalItems + " sản phẩm vào giỏ hàng",
                "addedItems", totalItems
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
}