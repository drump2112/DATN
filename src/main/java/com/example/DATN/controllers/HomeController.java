package com.example.DATN.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	@GetMapping("/home")
	public String showHomePage() {
		return "admin/dashboard";
	}

	@GetMapping("/")
	public String showShopPage() {
		return "shop/index";
	}
}
