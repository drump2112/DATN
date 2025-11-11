package com.example.DATN.controllers.customer;

import com.example.DATN.services.OrderService;
import com.example.DATN.enums.OrderStatus;
import com.example.DATN.models.Order;
import com.example.DATN.models.OrderItem;
import com.example.DATN.dtos.CartItemDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customer/orders")
public class CustomerOrderActionController {

    @Autowired
    private OrderService orderService;

    // Khách hàng hủy đơn hàng (PENDING -> CANCELLED)

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
                        "message", "Đơn hàng không tồn tại"));
            }

            // Kiểm tra quyền sở hữu đơn hàng (nếu có authentication)
            if (authentication != null) {
                String currentUsername = authentication.getName();
                if (!order.getUser().getUserName().equals(currentUsername)) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Bạn không có quyền thao tác đơn hàng này"));
                }
            }

            // Kiểm tra trạng thái có thể hủy không
            if (!OrderStatus.PENDING.name().equals(order.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Chỉ có thể hủy đơn hàng ở trạng thái chờ xử lý"));
            }

            // Cập nhật trạng thái
            orderService.updateOrderStatus(orderId, OrderStatus.CANCELLED.name());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã hủy đơn hàng thành công"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }

    // Khách hàng xác nhận đã nhận hàng (SHIPPING -> COMPLETED)
    @PostMapping("/{orderId}/confirm-received")
    public ResponseEntity<Map<String, Object>> confirmReceived(
            @PathVariable Integer orderId,
            Authentication authentication) {

        try {
            Order order = orderService.findById(orderId);
            if (order == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Đơn hàng không tồn tại"));
            }

            // Kiểm tra quyền sở hữu
            if (authentication != null) {
                String currentUsername = authentication.getName();
                if (!order.getUser().getUserName().equals(currentUsername)) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Bạn không có quyền thao tác đơn hàng này"));
                }
            }

            // Kiểm tra trạng thái
            if (!OrderStatus.SHIPPING.name().equals(order.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Chỉ có thể xác nhận đơn hàng ở trạng thái đang giao hàng"));
            }

            // Cập nhật trạng thái
            orderService.updateOrderStatus(orderId, OrderStatus.COMPLETED.name());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã xác nhận nhận hàng thành công. Cảm ơn bạn đã mua sắm!"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/return")
    public ResponseEntity<Map<String, Object>> requestReturn(
            @PathVariable Integer orderId,
            Authentication authentication) {

        try {
            Order order = orderService.findById(orderId);
            if (order == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Đơn hàng không tồn tại"));
            }

            // Kiểm tra quyền sở hữu
            if (authentication != null) {
                String currentUsername = authentication.getName();
                if (!order.getUser().getUserName().equals(currentUsername)) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Bạn không có quyền thao tác đơn hàng này"));
                }
            }

            // Kiểm tra trạng thái
            if (!OrderStatus.COMPLETED.name().equals(order.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Chỉ có thể yêu cầu đổi/trả đơn hàng đã hoàn thành"));
            }

            // Cập nhật trạng thái
            orderService.updateOrderStatus(orderId, OrderStatus.RETURN.name());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã gửi yêu cầu đổi/trả hàng. Chúng tôi sẽ liên hệ với bạn sớm nhất."));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }

    // Mua lại đơn hàng - thêm tất cả sản phẩm vào giỏ hàng
    @PostMapping("/{orderCode}/reorder")
    public ResponseEntity<Map<String, Object>> reorderItems(
            @PathVariable String orderCode,
            Authentication authentication,
            HttpSession session) {

        try {
            // Tìm đơn hàng theo orderCode với items
            Order order = orderService.findByOrderCodeWithItems(orderCode);
            if (order == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Đơn hàng không tồn tại"));
            }

            // Kiểm tra quyền sở hữu
            if (authentication != null) {
                String currentUsername = authentication.getName();
                if (!order.getUser().getUserName().equals(currentUsername)) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Bạn không có quyền thao tác đơn hàng này"));
                }
            }

            // Kiểm tra đơn hàng có items không
            if (order.getItems() == null || order.getItems().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Đơn hàng không có sản phẩm nào để mua lại"));
            }
            @SuppressWarnings("unchecked")
            List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute("CART_ITEMS");
            if (cart == null) {
                cart = new ArrayList<>();
            }

            System.out.println("Reorder - Current cart size: " + cart.size());
            System.out.println("Reorder - Session ID: " + session.getId());
            System.out.println("Reorder - Order has " + order.getItems().size() + " items");

            // Thêm các sản phẩm từ đơn hàng vào giỏ hàng
            int addedItems = 0;
            for (OrderItem item : order.getItems()) {
                System.out.println("Processing item: " + item.getProductVariant().getProduct().getName());

                // Tạo CartItemDTO từ OrderItem
                CartItemDTO cartItem = new CartItemDTO();
                cartItem.setVariantId(item.getProductVariant().getId());
                cartItem.setQuantity(item.getQuantity());
                cartItem.setVariantCode(item.getProductVariant().getVariantCode());
                cartItem.setPrice(item.getUnitPrice().doubleValue());
                cartItem.setName(item.getProductVariant().getProduct().getName());
                cartItem.setColorId(item.getProductVariant().getColor().getId());
                cartItem.setColorName(item.getProductVariant().getColor().getName());
                cartItem.setSizeId(item.getProductVariant().getSize().getId());
                cartItem.setSizeName(item.getProductVariant().getSize().getName());

                // Thiết lập image nếu có
                if (item.getProductVariant().getProduct().getProductVariantImages() != null &&
                        !item.getProductVariant().getProduct().getProductVariantImages().isEmpty()) {
                    String imageUrl = item.getProductVariant().getProduct().getProductVariantImages().get(0)
                            .getImageUrl();
                    cartItem.setImage(imageUrl);
                    System.out.println("Set image: " + imageUrl);
                } else {
                    System.out.println("No images available for product");
                }

                // Thiết lập maxQuantity từ inventory
                cartItem.setMaxQuantity(item.getProductVariant().getQuantity());

                System.out.println("Created CartItem: " + cartItem.getName() + " - Qty: " + cartItem.getQuantity());

                boolean found = false;
                for (CartItemDTO existing : cart) {
                    if (existing.getVariantId().equals(cartItem.getVariantId())) {
                        // Cộng dồn số lượng
                        existing.setQuantity(existing.getQuantity() + cartItem.getQuantity());
                        found = true;
                        break;
                    }
                }

                // Nếu chưa có thì thêm mới
                if (!found) {
                    cart.add(cartItem);
                }

                addedItems++; // Đếm mỗi item được xử lý
            }

            // Lưu lại giỏ hàng vào session
            session.setAttribute("CART_ITEMS", cart);

            System.out.println("Reorder - Final cart size: " + cart.size());
            System.out.println("Reorder - Added items: " + addedItems);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã thêm " + addedItems + " sản phẩm vào giỏ hàng",
                    "addedItems", addedItems));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }
}