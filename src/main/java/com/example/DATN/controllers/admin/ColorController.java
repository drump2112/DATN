package com.example.DATN.controllers.admin;

import com.example.DATN.dtos.ColorDTO;
import com.example.DATN.services.ColorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("admin/color")
public class ColorController {

	@Autowired
	ColorService colorService;

	@GetMapping("/")
	public String getAllColor(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size,
			Model model) {

		Page<ColorDTO> colorsPage = colorService.findAll(page, size);

		model.addAttribute("colors", colorsPage.getContent());
		model.addAttribute("currentPage", colorsPage.getNumber());
		model.addAttribute("totalPages", colorsPage.getTotalPages());
		model.addAttribute("totalItems", colorsPage.getTotalElements());
		model.addAttribute("pageSize", colorsPage.getSize());

		return "admin/color/list";

	}
}
