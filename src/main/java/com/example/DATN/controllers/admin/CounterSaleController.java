package com.example.DATN.controllers.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/buyatthecounter")
public class CounterSaleController {

	@GetMapping("/")
	public String getPageCounterSale() {
		return "admin/buyatthecounter/buyatthecounter";
	}

}
