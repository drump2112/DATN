package com.example.DATN.controllers.admin;

import java.util.Map;

import com.example.DATN.dtos.ProductVariantDTO;
import com.example.DATN.request.ProductVariantRequest;
import com.example.DATN.services.ProductVariantService;

import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping("/admin/productVariant")
public class ProductVariantController {

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

		return "admin/productVariant/list";
	}

	@GetMapping("/table")
	public String getTableFragment(

			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size,
			Model model) {
		Page<ProductVariantDTO> productviVariantPage = productVariantService.getAllProducts(page, size);

		log.info("Record" + productviVariantPage.getContent().size());
		model.addAttribute("listProducts", productviVariantPage.getContent());
		model.addAttribute("currentPage", productviVariantPage.getNumber());
		model.addAttribute("totalPages", productviVariantPage.getTotalPages());
		model.addAttribute("totalItems", productviVariantPage.getTotalElements());
		model.addAttribute("pageSize", productviVariantPage.getSize());

		return "admin/productVariant/table :: table";
	}

	@PostMapping("/add")
	public ResponseEntity<?> addProductVariant(@ModelAttribute ProductVariantRequest req) {
		productVariantService.addProductVariant(req);
		return ResponseEntity.ok(Map.of("message", "Thêm thành công"));
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
		Page<ProductVariantDTO> productsPage = productVariantService.searchProductVariants(
				keyword, colorId, sizeId, cateId, status, pageable);

		model.addAttribute("listProducts", productsPage.getContent());
		model.addAttribute("currentPage", productsPage.getNumber());
		model.addAttribute("totalPages", productsPage.getTotalPages());
		model.addAttribute("totalItems", productsPage.getTotalElements());
		model.addAttribute("pageSize", productsPage.getSize());

		return "admin/productVariant/table :: table";
	}

	@GetMapping("/count")
	@ResponseBody
	public long countProductVariants() {
		return productVariantService.countAll();
	}

	@PutMapping("/{id}/toggle-status")
	public ResponseEntity<?> toggleVariantStatus(@PathVariable Integer id) {
		boolean newStatus = productVariantService.toggleStatus(id);
		String message = newStatus ? "Kích hoạt sản phẩm thành công" : "Đã khóa sản phẩm";
		return ResponseEntity.ok(Map.of("message", message));
	}

}
