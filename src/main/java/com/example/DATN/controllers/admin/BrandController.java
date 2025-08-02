package com.example.DATN.controllers.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.DATN.dtos.BrandDTO;
import com.example.DATN.request.BrandRequest;
import com.example.DATN.services.BrandService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/admin/brand")
public class BrandController {

	@Autowired
	BrandService brandService;

	@GetMapping("/")
	public String getAllBrand(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size,
			Model model) {

		Page<BrandDTO> brandsPage = brandService.findAll(page, size);

		model.addAttribute("brands", brandsPage.getContent());
		model.addAttribute("currentPage", brandsPage.getNumber());
		model.addAttribute("totalPages", brandsPage.getTotalPages());
		model.addAttribute("totalItems", brandsPage.getTotalElements());
		model.addAttribute("pageSize", brandsPage.getSize());

		return "admin/brand/list";
	}

	@GetMapping("/select2")
	@ResponseBody
	public List<Map<String, Object>> getBrandsForSelect2(@RequestParam(required = false) String q) {
		List<BrandDTO> brands = brandService.getBrands(q);

		return brands.stream()
				.map(brand -> {
					Map<String, Object> item = new HashMap<>();
					item.put("id", brand.getId());
					item.put("text", brand.getName());
					return item;
				})
				.collect(Collectors.toList());
	}

	@PostMapping("/add")
	public ResponseEntity<?> addBrand(
			@ModelAttribute BrandRequest brandRequest) {
		try {
			boolean success = brandService.addBrand(brandRequest);
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

}
