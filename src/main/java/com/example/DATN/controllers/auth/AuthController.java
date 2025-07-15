package com.example.DATN.controllers.auth;

import java.time.LocalDateTime;

import com.example.DATN.models.User;
import com.example.DATN.models.VerificationToken;
import com.example.DATN.repositories.UserRepository;
import com.example.DATN.repositories.VerificationTokenRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
	public String showLoginPage() {
		return "admin/auth/login";
	}
}
