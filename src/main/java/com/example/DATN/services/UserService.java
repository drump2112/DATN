package com.example.DATN.services;

import com.example.DATN.dtos.UserDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

	Page<UserDTO> findAll(int page, int size);

	Page<UserDTO> getAllCustomer(int page, int size);

	Page<UserDTO> getAllEmployee(int page, int size);

	boolean toggleStatus(Integer id);

	boolean addEmployee(UserDTO dto, MultipartFile avatar);

	Page<UserDTO> searchUsers(String keyword, Boolean isActive, Pageable pageable);
}
