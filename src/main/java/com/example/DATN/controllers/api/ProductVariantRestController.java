package com.example.DATN.controllers.api;

import com.example.DATN.dtos.ProductVariantDTO;
import com.example.DATN.services.ProductVariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.DATN.dtos.ProductVariantDTO;
import com.example.DATN.services.ProductVariantService;

import lombok.RequiredArgsConstructor;

import java.util.List;

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

}
