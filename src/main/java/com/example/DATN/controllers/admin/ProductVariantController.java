package com.example.DATN.controllers.admin;

import com.example.DATN.dtos.ProductVariantDTO;
import com.example.DATN.exception.BusinessException;
import com.example.DATN.request.ProductVariantRequest;
import com.example.DATN.request.ProductVariantUpdateRequest;
import com.example.DATN.services.ProductVariantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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

  @GetMapping("/{id}")
  public ResponseEntity<ProductVariantDTO> getById(@PathVariable Integer id) {
    return ResponseEntity.ok(productVariantService.findById(id));
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
  public ResponseEntity<?> addProductVariant(
      @ModelAttribute ProductVariantRequest req,
      @RequestParam Map<String, String> allParams) {
    try {
      // Xử lý quantities và skus
      Map<Integer, Integer> quantities = new HashMap<>();
      // Map<Integer, String> skus = new HashMap<>();
      for (Map.Entry<String, String> entry : allParams.entrySet()) {
        String key = entry.getKey();
        String value = entry.getValue();
        if (key.startsWith("quantities[")) {
          Integer sizeId = Integer.parseInt(key.replaceAll("quantities\\[(\\d+)\\]", "$1"));
          quantities.put(sizeId, Integer.parseInt(value));
        }
      }
      req.setQuantities(quantities);
      // req.setSkus(skus);

      // Validate
      if (req.getSizeIds() == null || req.getSizeIds().isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of("message", "Phải chọn ít nhất một kích cỡ"));
      }

      for (Integer sizeId : req.getSizeIds()) {
        if (!quantities.containsKey(sizeId)) {
          return ResponseEntity.badRequest()
              .body(Map.of("message", "Số lượng hoặc SKU không đầy đủ cho kích cỡ " + sizeId));
        }
      }

      // Gọi service
      productVariantService.addProductVariant(req);

      return ResponseEntity.ok(Map.of("message", "Thêm thành công"));
    } catch (NumberFormatException e) {
      return ResponseEntity.badRequest().body(Map.of("message", "Dữ liệu số không hợp lệ"));
    } catch (BusinessException e) {
      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(500).body(Map.of("message", "Lỗi server: " + e.getMessage()));
    }
  }

  @PutMapping("/{id}/update")
  public ResponseEntity<?> updateProductVariant(
      @PathVariable Integer id,
      @ModelAttribute ProductVariantUpdateRequest req) {
    try {
      productVariantService.updateProductVariant(id, req);
      return ResponseEntity.ok(Map.of("message", "Cập nhật thành công"));
    } catch (BusinessException e) {
      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(500).body(Map.of("message", "Lỗi server: " + e.getMessage()));
    }
  }

  @GetMapping("/search")
  public String searchProducts(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer colorId,
      @RequestParam(required = false) Integer sizeId,
      @RequestParam(required = false) Integer cateId,
      @RequestParam(required = false) Integer brandId,
      @RequestParam(required = false) Boolean status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "5") int size,
      Model model) {

    Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
    Page<ProductVariantDTO> productsPage = productVariantService.searchProductVariants(
        keyword, colorId, sizeId, cateId, brandId, status, pageable);

    model.addAttribute("listProducts", productsPage.getContent());
    model.addAttribute("currentPage", productsPage.getNumber());
    model.addAttribute("totalPages", productsPage.getTotalPages());
    model.addAttribute("totalItems", productsPage.getTotalElements());
    model.addAttribute("pageSize", productsPage.getSize());

    return "admin/productVariant/table :: table";
  }

  @GetMapping("/productvariant-list/{id}")
  public ResponseEntity<?> productVariant_List(@PathVariable Integer id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "5") int size,
      Model model) {
    // getlist by id của produc
    Page<ProductVariantDTO> productviVariantPage = productVariantService.getVariantsByProductId(size, page, id);

    Map<String, Object> response = new HashMap<>();
    response.put("listProducts", productviVariantPage.getContent());
    response.put("currentPage", productviVariantPage.getNumber());
    response.put("totalPages", productviVariantPage.getTotalPages());
    response.put("totalItems", productviVariantPage.getTotalElements());
    response.put("pageSize", productviVariantPage.getSize());
    return ResponseEntity.ok(response);
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
