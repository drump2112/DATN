package com.example.DATN.services;

import com.example.DATN.dtos.RoleDTO;

import org.springframework.data.domain.Page;

public interface RoleService {
	Page<RoleDTO> findAll(int page, int size);

}
