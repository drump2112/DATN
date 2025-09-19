package com.example.DATN.services;

import java.util.List;

import com.example.DATN.dtos.BrandDTO;
import com.example.DATN.dtos.ColorDTO;
import com.example.DATN.dtos.ProductVariantDTO;
import com.example.DATN.request.BrandRequest;

import com.example.DATN.request.SizeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BrandService {

	Page<BrandDTO> findAll(int page, int size);

	boolean addBrand(BrandRequest brandRequest);

	List<BrandDTO> getBrands(String keyword);

    boolean updateBrand(Integer id, BrandRequest brandRequest);

    BrandDTO findById(Integer id);

    boolean toggleStatus(Integer id);

    Page<BrandDTO> searchBrand(String keyword, Boolean isActive, Pageable pageable);
}
