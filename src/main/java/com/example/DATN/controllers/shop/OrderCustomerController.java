package com.example.DATN.controllers.shop;

import com.example.DATN.models.User;
import com.example.DATN.repositories.UserRepository;
import com.example.DATN.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
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

        var orders = orderService.getUserOrders(Integer.valueOf(user.getId()), page, size);

        model.addAttribute("listorders", orders.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("totalItems", orders.getTotalElements());

        return "shop/orders";
    }
}
