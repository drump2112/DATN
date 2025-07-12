package com.example.DATN.controllers.admin;

import com.example.DATN.dtos.BrandDTO;
import com.example.DATN.services.BrandService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

}
