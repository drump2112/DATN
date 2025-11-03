package com.example.DATN.controllers.shop;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import com.example.DATN.request.CustomerRequest;
import com.example.DATN.services.CustomerService;

@Controller
@RequestMapping("/customer/auth")
public class AuthCustomerController {

	@Autowired
	private CustomerService customerService;

	@GetMapping("/")
	public String getPageLogin(
			@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "redirectUrl", required = false) String redirectUrl,
			Model model,
			HttpSession session,
			Authentication authentication) {

			// Lưu redirectUrl vào session nếu có
			if (redirectUrl != null && !redirectUrl.isEmpty()) {
				session.setAttribute("REDIRECT_URL", redirectUrl);
			} else {
				// Nếu không có redirectUrl trong parameter, kiểm tra trong session
				String savedRedirectUrl = (String) session.getAttribute("REDIRECT_URL");
				if (savedRedirectUrl != null) {
					model.addAttribute("redirectUrl", savedRedirectUrl);
				}
			}

		if (authentication != null && authentication.isAuthenticated()) {
				String redirectTo = (String) session.getAttribute("REDIRECT_URL");
				if (redirectTo != null) {
					session.removeAttribute("REDIRECT_URL");
					return "redirect:" + redirectTo;
			}
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

	@PostMapping("/register")
	public ResponseEntity<?> registerCustomer(@ModelAttribute CustomerRequest customerRequest) {
		try {
			customerService.addCustomer(customerRequest);
			return ResponseEntity.ok(Map.of("message", "Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản."));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
		}
	}
}
