package com.example.DATN.controllers.admin;

import com.example.DATN.dtos.ProductVariantDTO;
import com.example.DATN.services.ProductVariantService;

import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping("admin/inventory")
public class InventoryController {

	@Autowired
	private ProductVariantService productVariantService;

	@GetMapping("/")
	public String getAllProductVariant(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size,
			Model model) {

		Page<ProductVariantDTO> productVariantPage = productVariantService.getAllProducts(page, size);

		model.addAttribute("pageTitle", "Danh sách sản phẩm chi tiết");
		model.addAttribute("listProducts", productVariantPage.getContent());
		model.addAttribute("currentPage", productVariantPage.getNumber());
		model.addAttribute("totalPages", productVariantPage.getTotalPages());
		model.addAttribute("totalItems", productVariantPage.getTotalElements());
		model.addAttribute("pageSize", productVariantPage.getSize());

		return "admin/inventory/list";
	}

	@GetMapping("/search")
	public String searchProducts(
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) Integer colorId,
			@RequestParam(required = false) Integer sizeId,
			@RequestParam(required = false) Integer cateId,
			@RequestParam(required = false) Boolean status,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size,
			Model model) {

		Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
		Page<ProductVariantDTO> productsPage = productVariantService.searchProductVariantsInventory(
				keyword, colorId, sizeId, cateId, pageable);

		model.addAttribute("listProducts", productsPage.getContent());
		model.addAttribute("currentPage", productsPage.getNumber());
		model.addAttribute("totalPages", productsPage.getTotalPages());
		model.addAttribute("totalItems", productsPage.getTotalElements());
		model.addAttribute("pageSize", productsPage.getSize());

		return "admin/inventory/table :: table";
	}

}
