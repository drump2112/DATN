package com.example.DATN.services;

import java.util.List;

import com.example.DATN.dtos.SizeDTO;

import org.springframework.data.domain.Page;

public interface SizeService {

	Page<SizeDTO> findAll(int page, int size);

	List<SizeDTO> getSizes(String keyword);

}
