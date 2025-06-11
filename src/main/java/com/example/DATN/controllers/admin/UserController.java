package com.example.DATN.controllers.admin;

import java.util.Map;

import com.example.DATN.dtos.UserDTO;
import com.example.DATN.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/user")
public class UserController {

	@Autowired
	private UserService userService;

	@GetMapping("/")
	public String getAllUser(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size,
			Model model) {
		Page<UserDTO> usersPage = userService.findAll(page, size);
		model.addAttribute("listUsers", usersPage.getContent());
		model.addAttribute("currentPage", usersPage.getNumber());
		model.addAttribute("totalPages", usersPage.getTotalPages());
		model.addAttribute("totalItems", usersPage.getTotalElements());
		model.addAttribute("pageSize", usersPage.getSize());

		return "admin/user/list";
	}

	@GetMapping("/customer")
	public String getAllCustomer(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size,
			Model model) {

		Page<UserDTO> usersPage = userService.getAllCustomer(page, size);

		model.addAttribute("listUsers", usersPage.getContent());
		model.addAttribute("currentPage", usersPage.getNumber());
		model.addAttribute("totalPages", usersPage.getTotalPages());
		model.addAttribute("totalItems", usersPage.getTotalElements());
		model.addAttribute("pageSize", usersPage.getSize());

		return "admin/user/customer/list";
	}

	@GetMapping("/employee")
	public String getAllEmpliyee(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size,
			Model model) {

		Page<UserDTO> usersPage = userService.getAllEmployee(page, size);

		model.addAttribute("listUsers", usersPage.getContent());
		model.addAttribute("currentPage", usersPage.getNumber());
		model.addAttribute("totalPages", usersPage.getTotalPages());
		model.addAttribute("totalItems", usersPage.getTotalElements());
		model.addAttribute("pageSize", usersPage.getSize());

		return "admin/user/employee/list";
	}

	@PutMapping("/customers/{id}/toggle-status")
	public ResponseEntity<?> toggleCustomerStatus(@PathVariable Integer id) {
		boolean newStatus = userService.toggleStatus(id);
		String message = newStatus ? "Kích hoạt tài khoản thành công" : "Đã khóa tài khoản";
		return ResponseEntity.ok(Map.of("message", message));
	}
}
