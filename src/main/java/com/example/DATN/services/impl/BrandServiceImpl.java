package com.example.DATN.services.impl;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.DATN.dtos.BrandDTO;
import com.example.DATN.dtos.ColorDTO;
import com.example.DATN.dtos.ProductVariantDTO;
import com.example.DATN.models.Brand;
import com.example.DATN.models.Color;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.repositories.BrandRepository;
import com.example.DATN.request.BrandRequest;
import com.example.DATN.services.BrandService;
import com.example.DATN.services.ImageService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BrandServiceImpl implements BrandService {

	@Autowired
	BrandRepository brandRepository;

	@Autowired
	ModelMapper modelMapper;

	@Autowired
	ImageService imageService;

	@Override
	public Page<BrandDTO> findAll(int page, int size) {

		Pageable pageable = PageRequest.of(page, size);

		Page<Brand> brand = brandRepository.findAll(pageable);

		return brand.map(entity -> modelMapper.map(entity, BrandDTO.class));
	}

	@Override
	public boolean addBrand(BrandRequest brandRequest) {
		try {
			Brand brand = fromRequest(brandRequest);

			brandRepository.save(brand);

			return true;
		} catch (Exception e) {
			throw new RuntimeException("Loi them thuong hieu: " + e.getMessage(), e);
		}
	}

	@Override
	public List<BrandDTO> getBrands(String keyword) {
		List<Brand> brands;

		if (keyword != null && !keyword.isBlank()) {
			brands = brandRepository.findByNameContainingIgnoreCase(keyword);
		} else {
			brands = brandRepository.findAll();
		}

		return brands.stream()
				.map(brand -> BrandDTO.builder()
						.id(brand.getId())
						.brandCode(brand.getBrandCode())
						.name(brand.getName())
						.logoUrl(brand.getLogoUrl())
						.build())
				.collect(Collectors.toList());
	}

    @Override
    public boolean updateBrand(Integer id, BrandRequest brandRequest) {
        return false;
    }

    @Override
    public BrandDTO findById(Integer id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("brand not found with id: " + id));
        return modelMapper.map(brand, BrandDTO.class);
    }

    @Override
    public boolean toggleStatus(Integer id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu"));
        brand.setIsActive(!brand.getIsActive());
        brandRepository.save(brand);
        return brand.getIsActive();
    }

    @Override
    public Page<BrandDTO> searchBrand(String keyword, Boolean isActive, Pageable pageable) {
        Page<Brand> brands = brandRepository.search(keyword, isActive, pageable);
        return brands.map(entity -> modelMapper.map(entity, BrandDTO.class));
    }

    public Brand fromRequest(BrandRequest req) {

		String brandCode = generateBrandCode();
		String logoUrl = "";
		if (req.getLogoUrl() != null && !req.getLogoUrl().isEmpty()) {
			logoUrl = uploadAvatar(req.getLogoUrl());
		}

		Brand.BrandBuilder brandBuilder = Brand.builder()
				.name(req.getName())
				.brandCode(brandCode)
				.logoUrl(logoUrl);
		return brandBuilder.build();
	}

	private String generateBrandCode() {
		Optional<Brand> lastBrand = brandRepository.findTopByOrderByIdDesc();
		int nextNumber = 1;

		if (lastBrand.isPresent()) {
			String lastCode = lastBrand.get().getBrandCode();
			String numberStr = lastCode.substring(2);

			try {
				nextNumber = Integer.parseInt(numberStr) + 1;
			} catch (NumberFormatException ignored) {
			}
		}
		return String.format("B-%03d", nextNumber);
	}

	private String uploadAvatar(MultipartFile avatar) {
		try {
			return imageService.saveImage(avatar, "brand");
		} catch (IOException e) {
			throw new RuntimeException("Lỗi khi lưu ảnh: " + e.getMessage(), e);
		}
	}

}
