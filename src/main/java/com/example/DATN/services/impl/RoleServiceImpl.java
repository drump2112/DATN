package com.example.DATN.services.impl;

import com.example.DATN.dtos.RoleDTO;
import com.example.DATN.models.Role;
import com.example.DATN.repositories.RoleRepository;
import com.example.DATN.services.RoleService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl implements RoleService {

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Override
	public Page<RoleDTO> findAll(int page, int size) {

		Pageable pageable = PageRequest.of(page, size);

		Page<Role> role = roleRepository.findAll(pageable);

		return role.map(entity -> modelMapper.map(entity, RoleDTO.class));
	}
}
