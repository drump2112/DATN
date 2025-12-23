package com.example.DATN.controllers.shop;

import com.example.DATN.dtos.CartItemDTO;
import com.example.DATN.models.User;
import com.example.DATN.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String getCheckoutPage(Model model, HttpSession session) {
        // Kiểm tra đăng nhập
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            // Lưu URL hiện tại vào session trước khi chuyển hướng
            session.setAttribute("REDIRECT_URL", "/checkout");
            System.out.println("CheckoutController - Setting REDIRECT_URL in session: /checkout");
            return "redirect:/customer/auth/";
        }

        // Lấy giỏ hàng từ session
        List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute("CART_ITEMS");
        if (cart == null || cart.isEmpty()) {
            return "redirect:/cart";
        }

        // Lấy thông tin người dùng từ authentication
        String username = authentication.getName();
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);

        // Tính toán tổng tiền
        double subtotal = cart.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        double shippingFee = 30000.0;
        double total = subtotal + shippingFee;

        model.addAttribute("cartItems", cart);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("total", total);

        return "shop/checkout";
    }
}