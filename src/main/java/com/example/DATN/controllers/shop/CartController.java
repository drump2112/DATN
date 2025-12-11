package com.example.DATN.controllers.shop;

import com.example.DATN.dtos.CartItemDTO;
import com.example.DATN.dtos.ProductVariantDTO;
import com.example.DATN.services.ProductVariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    private static final String CART_SESSION_KEY = "CART_ITEMS";

    @Autowired
    private ProductVariantService productVariantService;

    @GetMapping
    public String getCart() {
        return "shop/cart";
    }

    @GetMapping("/items")
    @ResponseBody
    public ResponseEntity<List<CartItemDTO>> getCartItems(HttpSession session) {
        List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute(CART_SESSION_KEY);
        System.out.println("Getting cart items, session ID: " + session.getId());
        System.out.println("Cart items count: " + (cart != null ? cart.size() : 0));
        return ResponseEntity.ok(cart != null ? cart : new ArrayList<>());
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<List<CartItemDTO>> addToCart(@RequestBody CartItemDTO item, HttpSession session) {
        @SuppressWarnings("unchecked")
        List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new ArrayList<>();
        }

        System.out.println("Adding item to cart, session ID: " + session.getId());
        System.out.println("Item: " + item.getVariantId() + " - Quantity: " + item.getQuantity());
        System.out.println("Current cart size: " + cart.size());

        // Kiểm tra sản phẩm đã tồn tại trong giỏ hàng chưa
        boolean found = false;
        for (CartItemDTO cartItem : cart) {
            if (cartItem.getVariantId().equals(item.getVariantId())) {
                System.out.println("Found existing item, updating quantity from " + cartItem.getQuantity() + " to "
                        + (cartItem.getQuantity() + item.getQuantity()));
                cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());
                // Cập nhật maxQuantity nếu có thay đổi
                if (item.getMaxQuantity() != null) {
                    cartItem.setMaxQuantity(item.getMaxQuantity());
                }
                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println("Adding new item to cart");
            cart.add(item);
        }

        session.setAttribute(CART_SESSION_KEY, cart);
        System.out.println("Cart updated, new size: " + cart.size());
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/update/{variantId}")
    @ResponseBody
    public ResponseEntity<?> updateCartItem(
            @PathVariable Integer variantId,
            @RequestParam int quantity,
            HttpSession session) {
        @SuppressWarnings("unchecked")
        List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute(CART_SESSION_KEY);

        if (cart != null) {
            for (CartItemDTO item : cart) {
                if (item.getVariantId().equals(variantId)) {
                    // Kiểm tra số lượng tối đa
                    if (item.getMaxQuantity() != null && quantity > item.getMaxQuantity()) {
                        System.out.println(
                                "Quantity validation failed: requested=" + quantity + ", max=" + item.getMaxQuantity());
                        return ResponseEntity.badRequest().body(Map.of(
                                "error", "QUANTITY_EXCEEDED",
                                "message",
                                "Số lượng yêu cầu (" + quantity + ") vượt quá số lượng còn lại ("
                                        + item.getMaxQuantity() + ")!",
                                "maxQuantity", item.getMaxQuantity(),
                                "requestedQuantity", quantity));
                    }

                    System.out.println("Updating quantity for variant " + variantId + " from " + item.getQuantity()
                            + " to " + quantity);
                    item.setQuantity(quantity);
                    break;
                }
            }
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return ResponseEntity.ok(cart != null ? cart : new ArrayList<>());
    }

    @DeleteMapping("/remove/{variantId}")
    @ResponseBody
    public ResponseEntity<List<CartItemDTO>> removeCartItem(
            @PathVariable Integer variantId,
            HttpSession session) {
        System.out.println("Attempting to remove item with variantId: " + variantId);
        @SuppressWarnings("unchecked")
        List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute(CART_SESSION_KEY);
        if (cart != null) {
            boolean removed = cart.removeIf(item -> item.getVariantId().equals(variantId));
            System.out.println("Item removed: " + removed);
            System.out.println("Cart size after removal: " + cart.size());
            session.setAttribute(CART_SESSION_KEY, cart);
        } else {
            System.out.println("Cart is null");
            cart = new ArrayList<>();
        }
        return ResponseEntity.ok(cart);
    }

    // Alternative POST method for removing items (in case DELETE is blocked)
    @PostMapping("/remove/{variantId}")
    @ResponseBody
    public ResponseEntity<List<CartItemDTO>> removeCartItemPost(
            @PathVariable Integer variantId,
            HttpSession session) {
        return removeCartItem(variantId, session);
    }

    @PostMapping("/clear")
    @ResponseBody
    public ResponseEntity<Map<String, String>> clearCart(HttpSession session) {
        System.out.println("Clearing cart from session: " + session.getId());
        session.removeAttribute(CART_SESSION_KEY);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Cart cleared"));
    }

    @GetMapping("/debug")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> debugCart(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute(CART_SESSION_KEY);

        Map<String, Object> debugInfo = Map.of(
                "sessionId", session.getId(),
                "cartSize", cart != null ? cart.size() : 0,
                "cartItems", cart != null ? cart : new ArrayList<>(),
                "sessionAttributes", session.getAttributeNames().toString());

        return ResponseEntity.ok(debugInfo);
    }

    @GetMapping("/variant/{variantId}/stock")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getVariantStock(@PathVariable Integer variantId) {
        // TODO: Implement logic to get current stock from ProductVariantService
        // For now, return mock data
        Map<String, Object> stockInfo = Map.of(
                "variantId", variantId,
                "availableQuantity", 50, // Mock data - should be from database
                "status", "available");

        return ResponseEntity.ok(stockInfo);
    }

    /**
     * Kiểm tra tồn kho của tất cả sản phẩm trong giỏ hàng trước khi checkout
     * @param session HttpSession chứa thông tin giỏ hàng
     * @return JSON với valid=true nếu tất cả sản phẩm đều còn hàng,
     *         hoặc valid=false kèm danh sách sản phẩm hết hàng/không đủ số lượng
     */
    @GetMapping("/validate-stock")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validateCartStock(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute(CART_SESSION_KEY);

        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> outOfStockItems = new ArrayList<>();

        if (cart == null || cart.isEmpty()) {
            response.put("valid", false);
            response.put("message", "Giỏ hàng trống");
            return ResponseEntity.ok(response);
        }

        for (CartItemDTO item : cart) {
            try {
                // Lấy thông tin tồn kho mới nhất từ database
                ProductVariantDTO variant = productVariantService.findById(item.getVariantId());

                if (variant == null) {
                    // Sản phẩm không tồn tại
                    Map<String, Object> outOfStockItem = new HashMap<>();
                    outOfStockItem.put("variantId", item.getVariantId());
                    outOfStockItem.put("productName", item.getName());
                    outOfStockItem.put("variantCode", item.getVariantCode());
                    outOfStockItem.put("requestedQuantity", item.getQuantity());
                    outOfStockItem.put("availableQuantity", 0);
                    outOfStockItems.add(outOfStockItem);
                } else {
                    int availableQuantity = variant.getQuantity() != null ? variant.getQuantity() : 0;

                    // Kiểm tra số lượng: nếu tồn kho <= 0 hoặc không đủ số lượng yêu cầu
                    if (availableQuantity <= 0 || availableQuantity < item.getQuantity()) {
                        Map<String, Object> outOfStockItem = new HashMap<>();
                        outOfStockItem.put("variantId", item.getVariantId());
                        outOfStockItem.put("productName", item.getName());
                        outOfStockItem.put("variantCode", item.getVariantCode());
                        outOfStockItem.put("requestedQuantity", item.getQuantity());
                        outOfStockItem.put("availableQuantity", availableQuantity);
                        outOfStockItems.add(outOfStockItem);

                        // Cập nhật maxQuantity trong cart item
                        item.setMaxQuantity(availableQuantity);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error checking stock for variant " + item.getVariantId() + ": " + e.getMessage());
                // Nếu có lỗi khi kiểm tra, coi như sản phẩm không khả dụng
                Map<String, Object> outOfStockItem = new HashMap<>();
                outOfStockItem.put("variantId", item.getVariantId());
                outOfStockItem.put("productName", item.getName());
                outOfStockItem.put("variantCode", item.getVariantCode());
                outOfStockItem.put("requestedQuantity", item.getQuantity());
                outOfStockItem.put("availableQuantity", 0);
                outOfStockItems.add(outOfStockItem);
            }
        }

        // Cập nhật lại session với thông tin maxQuantity mới
        session.setAttribute(CART_SESSION_KEY, cart);

        if (outOfStockItems.isEmpty()) {
            response.put("valid", true);
            response.put("message", "Tất cả sản phẩm đều còn hàng");
        } else {
            response.put("valid", false);
            response.put("message", "Một số sản phẩm đã hết hàng hoặc không đủ số lượng");
            response.put("outOfStockItems", outOfStockItems);
        }

        return ResponseEntity.ok(response);
    }
}
