package com.example.DATN.controllers.shop;

import com.example.DATN.dtos.CartItemDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    private static final String CART_SESSION_KEY = "CART_ITEMS";

    @GetMapping
    public String getCart() {
        return "shop/cart";
    }

    @GetMapping("/items")
    @ResponseBody
    public ResponseEntity<List<CartItemDTO>> getCartItems(HttpSession session) {
        List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute(CART_SESSION_KEY);
        return ResponseEntity.ok(cart != null ? cart : new ArrayList<>());
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<List<CartItemDTO>> addToCart(@RequestBody CartItemDTO item, HttpSession session) {
        List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new ArrayList<>();
        }

        // Kiểm tra sản phẩm đã tồn tại trong giỏ hàng chưa
        boolean found = false;
        for (CartItemDTO cartItem : cart) {
            if (cartItem.getVariantId().equals(item.getVariantId())) {
                cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());
                found = true;
                break;
            }
        }

        if (!found) {
            cart.add(item);
        }

        session.setAttribute(CART_SESSION_KEY, cart);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/update/{variantId}")
    @ResponseBody
    public ResponseEntity<List<CartItemDTO>> updateCartItem(
            @PathVariable Integer variantId,
            @RequestParam int quantity,
            HttpSession session) {
        List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute(CART_SESSION_KEY);
        if (cart != null) {
            for (CartItemDTO item : cart) {
                if (item.getVariantId().equals(variantId)) {
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
        System.out.println("Clearing cart from session");
        session.removeAttribute(CART_SESSION_KEY);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Cart cleared"));
    }
}
