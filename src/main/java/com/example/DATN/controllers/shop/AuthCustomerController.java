package com.example.DATN.controllers.shop;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.example.DATN.request.EmployeeRequest;
import com.example.DATN.services.UserService;

@Controller
@RequestMapping("/customer/auth")
public class AuthCustomerController {

	@Autowired
	private UserService userService;

	@GetMapping("/")
	public String getPageLogin() {
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
