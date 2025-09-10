package com.example.DATN.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping("admin/inventory")
public class InventoryController {

	@GetMapping("/")
	public String getAllInventory() {
		return "admin/inventory/list";
	}

}
