package com.example.DATN.services;

import java.util.List;

import com.example.DATN.dtos.ColorDTO;

import org.springframework.data.domain.Page;

public interface ColorService {

	Page<ColorDTO> findAll(int page, int size);

	List<ColorDTO> getColors(String keyword);

}
