package com.example.DATN.controllers.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.DATN.dtos.ProductDTO;
import com.example.DATN.request.ProductRequest;
import com.example.DATN.services.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping("/admin/product")
public class ProductController {

	@Autowired
	private ProductService productService;

	@GetMapping("/")
	public String getAllProducts(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size,
			Model model) {

		Page<ProductDTO> productsPage = productService.getAllProducts(page, size);

		model.addAttribute("pageTitle", "Danh sách sản phẩm");
		model.addAttribute("listProducts", productsPage.getContent());
		model.addAttribute("currentPage", productsPage.getNumber());
		model.addAttribute("totalPages", productsPage.getTotalPages());
		model.addAttribute("totalItems", productsPage.getTotalElements());
		model.addAttribute("pageSize", productsPage.getSize());

		return "admin/product/list";
	}

	@GetMapping("/table")
	public String getTableFragment(

			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size,
			Model model) {
		Page<ProductDTO> productsPage = productService.getAllProducts(page, size);

		log.info("Record" + productsPage.getContent().size());
		model.addAttribute("listProducts", productsPage.getContent());
		model.addAttribute("currentPage", productsPage.getNumber());
		model.addAttribute("totalPages", productsPage.getTotalPages());
		model.addAttribute("totalItems", productsPage.getTotalElements());
		model.addAttribute("pageSize", productsPage.getSize());

		return "admin/product/table :: table";
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProductDTO> getProduct(@PathVariable Integer id) {
		ProductDTO dto = productService.getProductDTOById(id);
		return ResponseEntity.ok(dto);
	}

	@GetMapping("/count")
	@ResponseBody
	public long countProduct(@RequestParam(required = false) String keyword) {
		return productService.countAll();
	}

	@PostMapping("/add")
	public ResponseEntity<?> addProduct(
			@ModelAttribute ProductRequest productRequest) {
		try {
			boolean result = productService.addProduct(productRequest);

			if (result) {
				return ResponseEntity.ok(Map.of("message", "Thêm Thành Công"));
			} else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Thêm thất bại"));

			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", e.getMessage()));

		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> updateProduct(
			@PathVariable Integer id,
			@ModelAttribute ProductRequest productRequest) {
		try {
			boolean result = productService.updateProduct(id, productRequest);

			if (result) {
				return ResponseEntity.ok(Map.of("message", "Cập Nhật Thành Công"));
			} else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Cập nhật thất bại"));
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Lỗi server: " + e.getMessage()));
		}
	}

	@GetMapping("/search")
	public String searchProducts(
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) Boolean isActive,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size,
			Model model) {

		Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
		Page<ProductDTO> productsPage = productService.searchProducts(keyword, isActive, pageable);

		model.addAttribute("listProducts", productsPage.getContent());
		model.addAttribute("currentPage", productsPage.getNumber());
		model.addAttribute("totalPages", productsPage.getTotalPages());
		model.addAttribute("totalItems", productsPage.getTotalElements());
		model.addAttribute("pageSize", productsPage.getSize());

		return "admin/product/table :: table"; // Trả về fragment
	}

	@PutMapping("/{id}/toggle-status")
	public ResponseEntity<?> toggleCustomerStatus(@PathVariable Integer id) {
		boolean newStatus = productService.toggleStatus(id);
		String message = newStatus ? "Mở khóa sản phẩm" : "Đã khóa sản phẩm";
		return ResponseEntity.ok(Map.of("message", message));
	}

	@GetMapping("/select2")
	@ResponseBody
	public List<Map<String, Object>> getProductForSelect2(@RequestParam(required = false) String q) {
		List<ProductDTO> products = productService.getProducts(q);
		return products.stream().map(
				product -> {
					Map<String, Object> item = new HashMap<>();
					item.put("id", product.getId());
					item.put("text", product.getName());
					return item;
				}).collect(Collectors.toList());
	}
}
