package com.example.DATN.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("admin/voucher")
public class VoucherController {

	@GetMapping("/")
	public String getListVoucher() {
		return "admin/voucher/list";
	}

	@PostMapping("/")
	public ResponseEntity<?> addVoucher() {
		return ResponseEntity.ok("ok");
	}
}
