package com.example.DATN.controllers.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.DATN.dtos.BrandDTO;
import com.example.DATN.dtos.ColorDTO;
import com.example.DATN.dtos.ProductDTO;
import com.example.DATN.dtos.ProductVariantDTO;
import com.example.DATN.request.BrandRequest;
import com.example.DATN.request.SizeRequest;
import com.example.DATN.services.BrandService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

        model.addAttribute("pageTitle", "Danh sách thương hiệu");
        model.addAttribute("brands", brandsPage.getContent());
        model.addAttribute("currentPage", brandsPage.getNumber());
        model.addAttribute("totalPages", brandsPage.getTotalPages());
        model.addAttribute("totalItems", brandsPage.getTotalElements());
        model.addAttribute("pageSize", brandsPage.getSize());

        return "admin/brand/list";
    }

    @GetMapping("/table")
    public String getTableFragment(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Page<BrandDTO> brandsPage = brandService.findAll(page, size);

        model.addAttribute("pageTitle", "Danh sách thương hiệu");
        model.addAttribute("brands", brandsPage.getContent());
        model.addAttribute("currentPage", brandsPage.getNumber());
        model.addAttribute("totalPages", brandsPage.getTotalPages());
        model.addAttribute("totalItems", brandsPage.getTotalElements());
        model.addAttribute("pageSize", brandsPage.getSize());

        return "admin/brand/table :: table";
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSize(
            @PathVariable Integer id,
            @ModelAttribute BrandRequest brandRequest) {
        try {
            boolean result = brandService.updateBrand(id, brandRequest);
            if (result) {
                return ResponseEntity.ok(Map.of("message", "Update Thành Công"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Thêm thất bại"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(brandService.findById(id));
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

    @GetMapping("/search")
    public String searchSize(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<BrandDTO> brands = brandService.searchBrand(keyword, isActive, pageable);

        model.addAttribute("brands", brands.getContent());
        model.addAttribute("currentPage", brands.getNumber());
        model.addAttribute("totalPages", brands.getTotalPages());
        model.addAttribute("totalItems", brands.getTotalElements());
        model.addAttribute("pageSize", brands.getSize());

        return "admin/brand/table :: table"; // Trả về fragment
    }

    @GetMapping("/counts")
	@ResponseBody
	public long countBrand(@RequestParam(required = false) String keyword) {
		return brandService.countAll();
	}

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleCustomerStatus(@PathVariable Integer id) {
        boolean newStatus = brandService.toggleStatus(id);
        String message = newStatus ? "Kích hoạt thương hiệu thành công" : "Đã khóa thương hiệu";
        return ResponseEntity.ok(Map.of("message", message));
    }

}
