package com.example.DATN.services.impl;

import com.example.DATN.dtos.UserDTO;
import com.example.DATN.models.User;
import com.example.DATN.repositories.UserRepository;
import com.example.DATN.services.UserService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Override
	public Page<UserDTO> findAll(int page, int size) {

		Pageable pageable = PageRequest.of(page, size);

		Page<User> user = userRepository.findAll(pageable);

		return user.map(entity -> {
			UserDTO dto = modelMapper.map(entity, UserDTO.class);
			if (entity.getRole() != null) {
				dto.setRoleId(entity.getRole().getId());
				dto.setRoleName(entity.getRole().getNameRole());
			}
			return dto;
		});
	}

	@Override
	public Page<UserDTO> getAllCustomer(int page, int size) {

		Pageable pageable = PageRequest.of(page, size);

		Page<User> user = userRepository.findByRoleId(3, pageable);

		return user.map(entity -> {
			UserDTO dto = modelMapper.map(entity, UserDTO.class);
			if (entity.getRole() != null) {
				dto.setRoleId(entity.getRole().getId());
				dto.setRoleName(entity.getRole().getNameRole());
			}
			return dto;
		});
	}

	@Override
	public Page<UserDTO> getAllEmployee(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);

		Page<User> user = userRepository.findByRoleIdNot(3, pageable);

		return user.map(entity -> {
			UserDTO dto = modelMapper.map(entity, UserDTO.class);
			if (entity.getRole() != null) {
				dto.setRoleId(entity.getRole().getId());
				dto.setRoleName(entity.getRole().getNameRole());
			}
			return dto;
		});

	}

	@Override
	public boolean toggleStatus(Integer id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
		user.setIsActive(!user.getIsActive());
		userRepository.save(user);
		return user.getIsActive();
	}

}
