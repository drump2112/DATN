package com.example.DATN.controllers.auth;

import java.time.LocalDateTime;

import com.example.DATN.models.User;
import com.example.DATN.models.VerificationToken;
import com.example.DATN.repositories.UserRepository;
import com.example.DATN.repositories.VerificationTokenRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

	@Autowired
	private VerificationTokenRepository tokenRepository;
	@Autowired
	private UserRepository userRepository;

	@GetMapping("/verify")
	public String verifyAccount(@RequestParam("token") String token) {
		VerificationToken vt = tokenRepository.findByToken(token);
		if (vt == null || vt.getExpiryDate().isBefore(LocalDateTime.now())) {
			return "redirect:/error?message=Token không hợp lệ hoặc đã hết hạn";
		}

		User user = vt.getUser();
		user.setIsActive(true);
		userRepository.save(user);

		tokenRepository.delete(vt);
		return "redirect:/login?verified=true";
	}

	@GetMapping("/login")
	public String showLoginPage(@RequestParam(value = "error", required = false) String error,
			Model model,
			Authentication authentication) {
		if (authentication != null && authentication.isAuthenticated()) {
			return "redirect:/home";
		}

		if ("disabled".equals(error)) {
			model.addAttribute("errorMsg", "Tài khoản của bạn chưa được kích hoạt.");
		} else if ("bad".equals(error)) {
			model.addAttribute("errorMsg", "Sai tên đăng nhập hoặc mật khẩu.");
		}
		return "admin/auth/login";
	}
}
