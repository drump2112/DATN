package com.example.DATN.controllers.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.DATN.dtos.SizeDTO;
import com.example.DATN.services.SizeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/admin/size")
public class SizeController {

	@Autowired
	private SizeService sizeService;

	@GetMapping("/")
	public String getAllSize(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size,
			Model model) {

		Page<SizeDTO> sizePage = sizeService.findAll(page, size);

		model.addAttribute("pageTitle", "Danh sách kích cỡ");
		model.addAttribute("sizes", sizePage.getContent());
		model.addAttribute("currentPage", sizePage.getNumber());
		model.addAttribute("totalPages", sizePage.getTotalPages());
		model.addAttribute("totalItems", sizePage.getTotalElements());
		model.addAttribute("pageSize", sizePage.getSize());

		return "admin/size/list";

	}

	@GetMapping("/select2")
	@ResponseBody
	public List<Map<String, Object>> getSizeForSelect2(@RequestParam(required = false) String q) {
		List<SizeDTO> sizes = sizeService.getSizes(q);

		return sizes.stream()
				.map(size -> {
					Map<String, Object> item = new HashMap<>();
					item.put("id", size.getId());
					item.put("text", size.getName());
					return item;
				})
				.collect(Collectors.toList());
	}

}
