package com.example.DATN.controllers.shop;

import java.util.List;

import com.example.DATN.dtos.ProductDTO;
import com.example.DATN.dtos.ProductVariantDTO;
import com.example.DATN.services.ProductService;
import com.example.DATN.services.ProductVariantService;
import com.example.DATN.services.ColorService;
import com.example.DATN.services.BrandService;
import com.example.DATN.services.SizeService;
import com.example.DATN.dtos.ColorDTO;
import com.example.DATN.dtos.BrandDTO;
import com.example.DATN.dtos.SizeDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class HomeController {

	@Autowired
	private ProductService service;

	@Autowired
	private ProductVariantService variantService;

	@Autowired
	private ColorService colorService;

	@Autowired
	private BrandService brandService;

	@Autowired
	private SizeService sizeService;

	@GetMapping("/")
	public String getProducts(Model model) {

		List<ProductDTO> list = service.getProductActive();
		List<ProductVariantDTO> bestSellingVariants = variantService.getBestSellingVariants(8);
		List<ProductVariantDTO> newestVariants = variantService.getNewestVariants(8);

		model.addAttribute("listProducts", list);
		model.addAttribute("bestSellingVariants", bestSellingVariants);
		model.addAttribute("newestVariants", newestVariants);

        // Populate filter options
        List<ColorDTO> colors = colorService.getColors("");
        List<BrandDTO> brands = brandService.getBrands("");
        List<SizeDTO> sizes = sizeService.getSizes("");
        model.addAttribute("colors", colors);
        model.addAttribute("brands", brands);
        model.addAttribute("sizes", sizes);

		return "shop/index";
	}

	@GetMapping("/shop")
	public String filterProducts(
			@RequestParam(value = "minPrice", required = false) Double minPrice,
			@RequestParam(value = "maxPrice", required = false) Double maxPrice,
			@RequestParam(value = "color", required = false) Integer colorId,
			@RequestParam(value = "brand", required = false) Integer brandId,
			@RequestParam(value = "size", required = false) Integer sizeId,
			Model model
	) {
		// Get all active products
		List<ProductDTO> products = service.getProductActive();

		// Filter by price, color, brand, size
		List<ProductDTO> filtered = products.stream()
			.filter(p -> {
				boolean matches = true;
				if (minPrice != null) matches &= p.getMinPrice() >= minPrice;
				if (maxPrice != null) matches &= p.getMaxPrice() <= maxPrice;
				return matches;
			})
			.filter(p -> {
				if (brandId != null) return p.getBrandId() != null && p.getBrandId().equals(brandId);
				return true;
			})
			.collect(java.util.stream.Collectors.toList());

		// For color and size, filter at variant level
		if (colorId != null || sizeId != null) {
			filtered = filtered.stream()
				.filter(p -> {
					// Get variants for product
					List<ProductVariantDTO> variants = variantService.getVariantsByProductId(p.getId());
					return variants.stream().anyMatch(v ->
						(colorId == null || (v.getColorId() != null && v.getColorId().equals(colorId))) &&
						(sizeId == null || (v.getSizeId() != null && v.getSizeId().equals(sizeId)))
					);
				})
				.collect(java.util.stream.Collectors.toList());
		}

		List<ProductVariantDTO> bestSellingVariants = variantService.getBestSellingVariants(8);
		List<ProductVariantDTO> newestVariants = variantService.getNewestVariants(8);

		// Populate filter options
		List<ColorDTO> colors = colorService.getColors("");
		List<BrandDTO> brands = brandService.getBrands("");
		List<SizeDTO> sizes = sizeService.getSizes("");

		model.addAttribute("listProducts", filtered);
		model.addAttribute("bestSellingVariants", bestSellingVariants);
		model.addAttribute("newestVariants", newestVariants);
		model.addAttribute("colors", colors);
		model.addAttribute("brands", brands);
		model.addAttribute("sizes", sizes);

		// Preserve filter values in form
		model.addAttribute("minPrice", minPrice);
		model.addAttribute("maxPrice", maxPrice);
		model.addAttribute("color", colorId);
		model.addAttribute("brand", brandId);
		model.addAttribute("size", sizeId);

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
