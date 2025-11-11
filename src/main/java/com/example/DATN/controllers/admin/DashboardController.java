package com.example.DATN.controllers.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.DATN.dtos.DashboardStatsDto;
import com.example.DATN.services.DashboardService;

@Controller
@RequestMapping("/admin")
public class DashboardController {

	@Autowired
	private DashboardService dashboardService;

	@GetMapping("/home")
	public String showHomePage(Model model) {
		model.addAttribute("pageTitle", "Dashboard - Quản trị");
		return "admin/dashboard";
	}

	@GetMapping("/api/dashboard/stats")
	@ResponseBody
	public DashboardStatsDto getDashboardStats() {
		return dashboardService.getDashboardStats();
	}

}
