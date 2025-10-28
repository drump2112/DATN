package com.example.DATN.controllers.shop;

import com.example.DATN.dtos.OrderDTO;
import com.example.DATN.models.Order;
import com.example.DATN.models.User;
import com.example.DATN.repositories.UserRepository;
import com.example.DATN.services.OrderService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/orders")
public class OrderCustomerController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String getOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByUserNameOrEmail(userDetails.getUsername(), userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("=== FETCHING ORDERS FOR USER ===");
        System.out.println("User ID: " + user.getId());
        System.out.println("Username: " + user.getUserName());

        Page<OrderDTO> orders = orderService.getUserOrders(Integer.valueOf(user.getId()), page, size);

        System.out.println("=== ORDERS RESULT ===");
        System.out.println("Total elements: " + orders.getTotalElements());
        System.out.println("Total pages: " + orders.getTotalPages());
        System.out.println("Current page: " + orders.getNumber());
        System.out.println("Content size: " + orders.getContent().size());

        if (!orders.getContent().isEmpty()) {
            System.out.println("=== FIRST ORDER DETAILS ===");
            var firstOrder = orders.getContent().get(0);
            System.out.println("Order Code: " + firstOrder.getOrderCode());
            System.out.println("Total Amount: " + firstOrder.getTotalAmount());
            System.out.println("Discount: " + firstOrder.getDiscountAmount());
            System.out.println("Final Amount: " + firstOrder.getFinalAmount());
            System.out.println("Status: " + firstOrder.getStatus());
            System.out.println("Payment Method: " + firstOrder.getPaymentMethod());
        } else {
            System.out.println("No orders found for user!");
        }

        model.addAttribute("listorders", orders.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("totalItems", orders.getTotalElements());

        return "shop/orders";
    }
}
