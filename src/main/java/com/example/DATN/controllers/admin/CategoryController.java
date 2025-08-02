package com.example.DATN.controllers.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.DATN.dtos.CategoryDTO;
import com.example.DATN.services.CategoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("admin/category")
public class CategoryController {

	@Autowired
	private CategoryService categoryService;

	@GetMapping("/")
	public String getListCategory(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size,
			Model model) {
		Page<CategoryDTO> catePage = categoryService.findAll(page, size);

		model.addAttribute("Categories", catePage.getContent());
		model.addAttribute("currentPage", catePage.getNumber());
		model.addAttribute("totalPages", catePage.getTotalPages());
		model.addAttribute("totalItems", catePage.getTotalElements());
		model.addAttribute("pageSize", catePage.getSize());

		return "admin/categories/list";
	}

	@GetMapping("/select2")
	@ResponseBody
	public List<Map<String, Object>> getCategoryForSelect2(@RequestParam(required = false) String q) {
		List<CategoryDTO> catetogries = categoryService.getCategories(q);

		return catetogries.stream()
				.map(category -> {
					Map<String, Object> item = new HashMap<>();
					item.put("id", category.getId());
					item.put("text", category.getName());
					return item;
				})
				.collect(Collectors.toList());
	}

	@PostMapping("/add")
	public ResponseEntity<?> addCategory(
			@RequestBody CategoryDTO categoryDTO) {

		return ResponseEntity.ok("oke");
	}
}
