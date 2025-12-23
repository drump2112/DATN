package com.example.DATN.controllers.api;

import com.example.DATN.dtos.ProductVariantDTO;
import com.example.DATN.services.ProductVariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.DATN.dtos.ProductVariantDTO;
import com.example.DATN.services.ProductVariantService;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/product-variants")
@RequiredArgsConstructor
public class ProductVariantRestController {

  @Autowired
  private ProductVariantService productVariantService;

  @GetMapping("/search")
  public List<ProductVariantDTO> search(@RequestParam String q) {
    return productVariantService.search(q);
  }

  @GetMapping("/variants")
  @ResponseBody
  public List<ProductVariantDTO> getAllVariants() {
    return productVariantService.findAllActive();
  }

  @PostMapping("/check-prices")
  @ResponseBody
  public Map<String, BigDecimal> checkPrices(@RequestBody Map<String, List<String>> request) {
    List<String> codes = request.get("variantCodes");
    if (codes == null) {
      codes = new ArrayList<>();
    }

    Map<String, BigDecimal> priceMap = new HashMap<>();

    for (String code : codes) {
      // Tìm variant theo code bằng cách search và filter
      List<ProductVariantDTO> results = productVariantService.search(code);
      ProductVariantDTO variant = results.stream()
          .filter(v -> code.equals(v.getVariantCode()))
          .findFirst()
          .orElse(null);

      if (variant != null && variant.getPrice() != null) {
        priceMap.put(code, variant.getPrice());
      }
    }

    return priceMap;
  }

}
