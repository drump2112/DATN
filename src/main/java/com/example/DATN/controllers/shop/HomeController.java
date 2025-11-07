package com.example.DATN.controllers.shop;

import java.util.List;

import com.example.DATN.dtos.ProductDTO;
import com.example.DATN.dtos.ProductVariantDTO;
import com.example.DATN.services.ProductService;
import com.example.DATN.services.ProductVariantService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class HomeController {

	@Autowired
	private ProductService service;

	@Autowired
	private ProductVariantService variantService;

	@GetMapping("/")
	public String getProducts(Model model) {

		List<ProductDTO> list = service.getProductActive();
		List<ProductVariantDTO> bestSellingVariants = variantService.getBestSellingVariants(8);
		List<ProductVariantDTO> newestVariants = variantService.getNewestVariants(8);

		model.addAttribute("listProducts", list);
		model.addAttribute("bestSellingVariants", bestSellingVariants);
		model.addAttribute("newestVariants", newestVariants);

		return "shop/index";
	}

	@GetMapping("/details/{id}")
	public String getAlLVariant(@PathVariable Integer id, Model model) {

		ProductDTO product = service.getById(id);

		log.info(product.toString());
		List<ProductVariantDTO> variants = variantService.getVariantsByProductId(id);

		log.info("=== Variants list ===");
		for (ProductVariantDTO variant : variants) {
			log.info(variant.toString());
		}
		model.addAttribute("product", product);
		try {
			String variantsJson = new ObjectMapper().writeValueAsString(variants);
			model.addAttribute("variants", variantsJson);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			model.addAttribute("variants", "[]");
		}

		return "shop/product-detail";
	}



}
