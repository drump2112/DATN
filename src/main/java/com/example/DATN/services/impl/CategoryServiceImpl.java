package com.example.DATN.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.example.DATN.dtos.CategoryDTO;
import com.example.DATN.dtos.CategoryDTO;
import com.example.DATN.exception.BusinessException;
import com.example.DATN.models.Category;
import com.example.DATN.models.Category;
import com.example.DATN.repositories.CategoryRepository;
import com.example.DATN.request.CategoryRequest;

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

	private static final String PREFIX = "Cate";

	@Override
	public Page<CategoryDTO> findAll(int page, int Category) {

		Pageable pageable = PageRequest.of(page, Category);

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

	public boolean toggleStatus(Integer id) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new BusinessException("Không tìm thấy danh mục"));
		category.setIsActive(!category.getIsActive());
		categoryRepository.save(category);
		return category.getIsActive();
	}

	@Override
	public boolean addCategory(CategoryRequest categoryRequest) {
		// Trim dữ liệu input
		categoryRequest.setName(categoryRequest.getName() != null ? categoryRequest.getName().trim() : null);

		if (categoryRepository.existsByName(categoryRequest.getName())) {
			throw new BusinessException("Danh mục đã tồn tại");
		}
		Category category = fromRequest(categoryRequest);
		categoryRepository.save(category);
		return true;
	}

	@Override
	public boolean updateCategory(Integer id, CategoryRequest categoryRequest) {
		// Trim dữ liệu input
		categoryRequest.setName(categoryRequest.getName() != null ? categoryRequest.getName().trim() : null);

		// Check tồn tại
		Category existingCategory = categoryRepository.findById(id)
				.orElseThrow(() -> new BusinessException("Không tìm thấy danh mục"));

		// Kiểm tra trùng tên (loại trừ chính danh mục hiện tại)
		if (!existingCategory.getName().equals(categoryRequest.getName()) &&
			categoryRepository.existsByName(categoryRequest.getName())) {
			throw new BusinessException("Tên danh mục đã tồn tại");
		}

		// Chỉ cập nhật name, giữ nguyên cateCode
		existingCategory.setName(categoryRequest.getName());
		// Không set cateCode vì nó không nên thay đổi

		categoryRepository.save(existingCategory);
		return true;
	}

	@Override
	public Page<CategoryDTO> searchCategory(String keyword, Boolean isActive, Pageable pageable) {
		Page<Category> category = categoryRepository.search(keyword, isActive, pageable);
		return category.map(entity -> modelMapper.map(entity, CategoryDTO.class));
	}

	public Category fromRequest(CategoryRequest req) {

		Category.CategoryBuilder CategoryBuilder = Category.builder()
				.cateCode(req.getCateCode())
				.name(req.getName())
				.isActive(true);

		String CategoryCode = generateCategoryCode();
		CategoryBuilder.cateCode(CategoryCode);
		return CategoryBuilder.build();
	}

	public String generateCategoryCode() {
		Long maxId = categoryRepository.findMaxId();
		if (maxId == null) {
			maxId = 0L;
		}
		return String.format("%s-%03d", PREFIX, maxId + 1);
	}

}
