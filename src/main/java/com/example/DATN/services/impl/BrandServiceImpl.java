package com.example.DATN.services.impl;

import com.example.DATN.dtos.BrandDTO;
import com.example.DATN.models.Brand;
import com.example.DATN.repositories.BrandRepository;
import com.example.DATN.services.BrandService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class BrandServiceImpl implements BrandService {

	@Autowired
	BrandRepository brandRepository;
	@Autowired
	ModelMapper modelMapper;

	@Override
	public Page<BrandDTO> findAll(int page, int size) {

		Pageable pageable = PageRequest.of(page, size);

		Page<Brand> brand = brandRepository.findAll(pageable);

		return brand.map(entity -> modelMapper.map(entity, BrandDTO.class));
	}
}
