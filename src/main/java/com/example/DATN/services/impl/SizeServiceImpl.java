package com.example.DATN.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.example.DATN.dtos.SizeDTO;
import com.example.DATN.models.Size;
import com.example.DATN.repositories.SizeRepository;
import com.example.DATN.services.SizeService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SizeServiceImpl implements SizeService {

	@Autowired
	private SizeRepository sizeRepository;
	@Autowired
	private ModelMapper modelMapper;

	@Override
	public Page<SizeDTO> findAll(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);

		Page<Size> sizes = sizeRepository.findAll(pageable);

		return sizes.map(entity -> modelMapper.map(entity, SizeDTO.class));

	}

	@Override
	public List<SizeDTO> getSizes(String keyword) {
		List<Size> sizes;

		if (keyword != null && !keyword.isBlank()) {
			sizes = sizeRepository.findByNameContainingIgnoreCase(keyword);
		} else {
			sizes = sizeRepository.findAll();
		}

		return sizes.stream()
				.map(size -> SizeDTO.builder()
						.id(size.getId())
						.name(size.getName())
						.build())
				.collect(Collectors.toList());
	}

}
