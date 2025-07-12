package com.example.DATN.services;

import com.example.DATN.dtos.BrandDTO;

import org.springframework.data.domain.Page;

public interface BrandService {

	Page<BrandDTO> findAll(int page, int size);

}
