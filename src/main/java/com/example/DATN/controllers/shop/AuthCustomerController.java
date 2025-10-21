package com.example.DATN.controllers.shop;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import com.example.DATN.request.EmployeeRequest;
import com.example.DATN.services.UserService;

@Controller
@RequestMapping("/customer/auth")
public class AuthCustomerController {

	@Autowired
	private UserService userService;

	@GetMapping("/")
	public String getPageLogin(@RequestParam(value = "error", required = false) String error,
			Model model,
			HttpSession session,
			Authentication authentication) {

		if (authentication != null && authentication.isAuthenticated()) {
			return "redirect:/";
		}

		Object lastUsername = session.getAttribute("lastUsername");

		if (lastUsername != null) {
			model.addAttribute("lastUsername", lastUsername);
			session.removeAttribute("lastUsername");
		}

		Object errorMsg = session.getAttribute("error");

		if (errorMsg != null) {
			model.addAttribute("errorMsg", errorMsg);
			session.removeAttribute("error");
		}

		if ("disabled".equals(error)) {
			model.addAttribute("errorMsg", "Tài khoản của bạn chưa được kích hoạt.");
		} else if ("bad".equals(error)) {
			model.addAttribute("errorMsg", "Sai tên đăng nhập hoặc mật khẩu.");
		}

		return "shop/auth/signin";
	}

	@GetMapping("/register")
	public String getPageRegister() {
		return "shop/auth/register";
	}

	@PostMapping("/add")
	public ResponseEntity<?> addCustomer(@ModelAttribute EmployeeRequest employee) {
		userService.addEmployee(employee);
		return ResponseEntity.ok(Map.of("message", "Thêm thành công"));
	}
}
