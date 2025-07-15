package com.example.DATN.controllers.admin;

import java.util.Map;

import com.example.DATN.dtos.UserDTO;
import com.example.DATN.request.EmployeeRequest;
import com.example.DATN.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin/employee")
public class UserController {

	@Autowired
	private UserService userService;

	@GetMapping("/")
	public String getAllEmployee(

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

	@GetMapping("/table")
	public String getTableFragment(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size,
			Model model) {
		Page<UserDTO> usersPage = userService.getAllEmployee(page, size);

		model.addAttribute("listUsers", usersPage.getContent());
		model.addAttribute("currentPage", usersPage.getNumber());
		model.addAttribute("totalPages", usersPage.getTotalPages());
		model.addAttribute("totalItems", usersPage.getTotalElements());
		model.addAttribute("pageSize", usersPage.getSize());

		return "admin/user/employee/table :: table";
	}

	@PostMapping("/add")
	public ResponseEntity<?> addEmployee(
			@ModelAttribute EmployeeRequest employee) {
		try {
			System.out.println("===Controller Call===");
			boolean success = userService.addEmployee(employee);
			if (success) {
				return ResponseEntity.ok(Map.of("message", "Thêm Thành Công"));
			} else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Thêm thất bại"));
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Lỗi server: " + e.getMessage()));
		}
	}

	@GetMapping("/search")
	public String searchUsers(

			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) Boolean isActive,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size,
			Model model) {

		Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
		Page<UserDTO> usersPage = userService.searchUsers(keyword, isActive, pageable);

		model.addAttribute("listUsers", usersPage);
		model.addAttribute("currentPage", usersPage.getNumber());
		model.addAttribute("totalPages", usersPage.getTotalPages());
		model.addAttribute("totalItems", usersPage.getTotalElements());
		model.addAttribute("pageSize", usersPage.getSize());

		return "admin/user/employee/table :: table"; // Trả về fragment
	}

	@PutMapping("/{id}/toggle-status")
	public ResponseEntity<?> toggleCustomerStatus(@PathVariable Integer id) {
		boolean newStatus = userService.toggleStatus(id);
		String message = newStatus ? "Kích hoạt tài khoản thành công" : "Đã khóa tài khoản";
		return ResponseEntity.ok(Map.of("message", message));
	}

}
