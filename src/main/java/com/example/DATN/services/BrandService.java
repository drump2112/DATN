package com.example.DATN.services;

import java.util.List;

import com.example.DATN.dtos.BrandDTO;
import com.example.DATN.request.BrandRequest;

import org.springframework.data.domain.Page;

public interface BrandService {

	Page<BrandDTO> findAll(int page, int size);

	boolean addBrand(BrandRequest brandRequest);

	List<BrandDTO> getBrands(String keyword);
}
