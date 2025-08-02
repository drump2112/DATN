package com.example.DATN.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.example.DATN.dtos.CategoryDTO;
import com.example.DATN.models.Category;
import com.example.DATN.repositories.CategoryRepository;
import com.example.DATN.services.CategoryService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl implements CategoryService {

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Override
	public Page<CategoryDTO> findAll(int page, int size) {

		Pageable pageable = PageRequest.of(page, size);

		Page<Category> category = categoryRepository.findAll(pageable);

		return category.map(entity -> modelMapper.map(entity, CategoryDTO.class));
	}

	@Override
	public List<CategoryDTO> getCategories(String keyword) {
		List<Category> categoriess;

		if (keyword != null && !keyword.isBlank()) {
			categoriess = categoryRepository.findByNameContainingIgnoreCase(keyword);
		} else {
			categoriess = categoryRepository.findAll();
		}

		return categoriess.stream()
				.map(category -> CategoryDTO.builder()
						.id(category.getId())
						.cateCode(category.getCateCode())
						.name(category.getName())
						.build())
				.collect(Collectors.toList());
	}

}
