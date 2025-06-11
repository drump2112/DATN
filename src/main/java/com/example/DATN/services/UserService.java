package com.example.DATN.services;

import com.example.DATN.dtos.UserDTO;

import org.springframework.data.domain.Page;

public interface UserService {
	Page<UserDTO> findAll(int page, int size);

	Page<UserDTO> getAllCustomer(int page, int size);

	Page<UserDTO> getAllEmployee(int page, int size);

	boolean toggleStatus(Integer id);
}
