package com.example.DATN.controllers.admin;

import com.example.DATN.dtos.RoleDTO;
import com.example.DATN.services.RoleService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("admin/role")
public class RoleController {

	@Autowired
	private RoleService roleService;

	@GetMapping("/")
	public String getListRole(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size,
			Model model) {

		Page<RoleDTO> rolesPage = roleService.findAll(page, size);

		model.addAttribute("roles", rolesPage.getContent());
		model.addAttribute("currentPage", rolesPage.getNumber());
		model.addAttribute("totalPages", rolesPage.getTotalPages());
		model.addAttribute("totalItems", rolesPage.getTotalElements());
		model.addAttribute("pageSize", rolesPage.getSize());

		return "admin/role/list";
	}
}
