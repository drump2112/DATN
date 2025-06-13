package com.example.DATN.services.impl;

import com.example.DATN.dtos.UserDTO;
import com.example.DATN.models.Role;
import com.example.DATN.models.User;
import com.example.DATN.repositories.RoleRepository;
import com.example.DATN.repositories.UserRepository;
import com.example.DATN.services.UserService;
import com.example.DATN.specifications.UserSpecification;

import org.apache.commons.logging.Log;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import groovy.util.logging.Log4j2;

@Service
@Log4j2
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

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

	@Override
	public boolean addEmployee(UserDTO dto) {
		try {

			User employee = fromDto(dto);
			System.out.println(employee.toString());
			userRepository.save(employee);

			return true;
		} catch (Exception e) {
			throw new RuntimeException("Loi them nhan vien: " + e.getMessage(), e);
		}
	};

	@Override
	public Page<UserDTO> searchUsers(String keyword, Boolean isActive, Pageable pageable) {
		Specification<User> spec = Specification
				.where(UserSpecification.containsKeyword(keyword))
				.and(UserSpecification.isActive(isActive))
				.and(UserSpecification.hasRoleIn(1, 2)); // Chỉ lấy roleId 1 hoặc 2

		Page<User> users = userRepository.findAll(spec, pageable);
		return users.map(user -> {
			UserDTO dto = new UserDTO();
			dto.setId(user.getId());
			dto.setUserCode(user.getUserCode());
			dto.setUserName(user.getUserName());
			dto.setEmail(user.getEmail());
			dto.setFullName(user.getFullName());
			dto.setAddress(user.getAddress());
			dto.setPhone(user.getPhone());
			dto.setRoleId(user.getRole().getId());
			dto.setRoleName(user.getRole().getNameRole());
			dto.setIsActive(user.getIsActive());
			return dto;
		});
	}

	public User fromDto(UserDTO dto) {
		User user = new User();

		user.setUserName(dto.getUserName());
		user.setFullName(dto.getFullName());
		user.setEmail(dto.getEmail());
		user.setPhone(dto.getPhone());
		user.setAddress(dto.getAddress());
		user.setPassword(!dto.getPassword().isEmpty() ? passwordEncoder.encode(dto.getPassword()) : null);
		Role role = roleRepository.findById(dto.getRoleId()).orElse(null);
		user.setRole(role);
		if (role != null) {
			String prefix = "";

			if (dto.getRoleId() == 1) {
				prefix = "AD";
				user.setIsActive(true);

			} else if (dto.getRoleId() == 2) {
				prefix = "NV";
				user.setIsActive(true);

			} else if (dto.getRoleId() == 3) {
				prefix = "KH";
				user.setIsActive(false);

			}

			if (!prefix.isEmpty()) {
				long count = userRepository.countByRoleId(dto.getRoleId());
				String code = String.format("%s-%03d", prefix, count + 1);
				user.setUserCode(code);
			}
		}
		return user;
	}
}
