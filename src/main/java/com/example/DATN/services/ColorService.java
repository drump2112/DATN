package com.example.DATN.services;

import com.example.DATN.dtos.ColorDTO;

import org.springframework.data.domain.Page;

public interface ColorService {

	Page<ColorDTO> findAll(int page, int size);

}
