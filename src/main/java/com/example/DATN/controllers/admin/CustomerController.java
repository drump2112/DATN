package com.example.DATN.controllers.admin;

import java.util.Map;

import com.example.DATN.dtos.UserDTO;
import com.example.DATN.exception.BusinessException;
import com.example.DATN.services.CustomerService;
import com.example.DATN.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/admin/customers")
public class CustomerController {

	@Autowired
	private UserService userService;

	@Autowired
	private CustomerService customerService;

	@GetMapping("/")
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

	@PutMapping("/{id}/toggle-status")
	public ResponseEntity<?> toggleCustomerStatus(@PathVariable Integer id) {
		boolean newStatus = userService.toggleStatus(id);
		String message = newStatus ? "Kích hoạt tài khoản thành công" : "Đã khóa tài khoản";
		return ResponseEntity.ok(Map.of("message", message));
	}

	@PostMapping("/quick-add")
	@ResponseBody
	public ResponseEntity<?> quickAddCustomer(@RequestParam String fullName, @RequestParam String phone) {
		try {
			customerService.addQuickCustomer(fullName, phone);
			return ResponseEntity.ok(Map.of("message", "Thêm khách hàng thành công!"));
		} catch (BusinessException e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(500).body(Map.of("message", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau."));
		}
	}
}
