package com.example.DATN.services.impl;

import com.example.DATN.dtos.ColorDTO;
import com.example.DATN.models.Color;
import com.example.DATN.repositories.ColorRepoSitory;
import com.example.DATN.services.ColorService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ColorServiceImpl implements ColorService {

	@Autowired
	private ColorRepoSitory colorRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Override
	public Page<ColorDTO> findAll(int page, int size) {

		Pageable pageable = PageRequest.of(page, size);

		Page<Color> color = colorRepository.findAll(pageable);

		return color.map(entity -> modelMapper.map(entity, ColorDTO.class));
	}

}
