package com.example.DATN.services;

import com.example.DATN.dtos.CategoryDTO;

import org.springframework.data.domain.Page;

public interface CategoryService {

	Page<CategoryDTO> findAll(int page, int size);

}
