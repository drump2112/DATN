package com.example.DATN.services;

import java.util.List;

import com.example.DATN.dtos.CategoryDTO;

import com.example.DATN.request.CategoryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {

    Page<CategoryDTO> findAll(int page, int size);


    List<CategoryDTO> getCategories(String keyword);


    boolean toggleStatus(Integer id);


    boolean addCategory(CategoryRequest categoryRequest);


    boolean updateCategory(Integer id, CategoryRequest categoryRequet);


    Page<CategoryDTO> searchCategory(String keyword, Boolean isActive, Pageable pageable);

}
