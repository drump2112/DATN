package com.example.DATN.controllers.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.DATN.dtos.ColorDTO;
import com.example.DATN.services.ColorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

		model.addAttribute("pageTitle", "Danh sách màu sắc");
		model.addAttribute("colors", colorsPage.getContent());
		model.addAttribute("currentPage", colorsPage.getNumber());
		model.addAttribute("totalPages", colorsPage.getTotalPages());
		model.addAttribute("totalItems", colorsPage.getTotalElements());
		model.addAttribute("pageSize", colorsPage.getSize());

		return "admin/color/list";

	}

	@GetMapping("/select2")
	@ResponseBody
	public List<Map<String, Object>> getColorForSelect2(@RequestParam(required = false) String q) {
		List<ColorDTO> colors = colorService.getColors(q);

		return colors.stream()
				.map(color -> {
					Map<String, Object> item = new HashMap<>();
					item.put("id", color.getId());
					item.put("text", color.getColorName());
					return item;
				})
				.collect(Collectors.toList());
	}

}
