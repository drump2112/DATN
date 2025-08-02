package com.example.DATN.services.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.example.DATN.configs.email.EmailService;
import com.example.DATN.dtos.UserDTO;
import com.example.DATN.exception.BusinessException;
import com.example.DATN.models.Role;
import com.example.DATN.models.User;
import com.example.DATN.models.VerificationToken;
import com.example.DATN.repositories.RoleRepository;
import com.example.DATN.repositories.UserRepository;
import com.example.DATN.repositories.VerificationTokenRepository;
import com.example.DATN.request.EmployeeRequest;
import com.example.DATN.services.ImageService;
import com.example.DATN.services.UserService;
import com.example.DATN.specifications.UserSpecification;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

	@Autowired
	private VerificationTokenRepository tokenRepository;

	@Autowired
	private EmailService emailService;

	@Autowired
	private ImageService imageService;

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
	public boolean addEmployee(EmployeeRequest employeeRequest) {
		List<Integer> checkRoles = List.of(1, 2);

		if (userRepository.existsByEmailAndRoleIdIn(employeeRequest.getEmail(), checkRoles)) {
			throw new BusinessException("Email đã được sử dụng.");
		}

		if (userRepository.existsByPhoneAndRoleIdIn(employeeRequest.getPhone(), checkRoles)) {
			throw new BusinessException("Số điện thoại đã được sử dụng.");
		}

		if (userRepository.existsByUserNameAndRoleIdIn(employeeRequest.getUserName(), checkRoles)) {
			throw new BusinessException("Tên đăng nhập đã tồn tại");
		}

		User employee = fromRequest(employeeRequest);

		userRepository.save(employee);

		String token = UUID.randomUUID().toString();
		VerificationToken verificationToken = new VerificationToken();
		verificationToken.setToken(token);
		verificationToken.setUser(employee);
		verificationToken.setExpiryDate(LocalDateTime.now().plusDays(1));

		tokenRepository.save(verificationToken);

		emailService.sendVerificationEmail(employee, token);

		return true;
	};

	@Override
	public boolean updateEmployee(Integer id, EmployeeRequest employeeRequest) {
		User existingUser = userRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng có id: " + id));

		Role newRole = roleRepository.findById(employeeRequest.getRoleId())
				.orElseThrow(
						() -> new RuntimeException("Không tìm thấy vai trò có id: " + employeeRequest.getRoleId()));

		String avatarPath = handleUploadAvatar(employeeRequest.getAvatar(), existingUser.getAvatar());

		User newUser = existingUser.toBuilder()
				.fullName(employeeRequest.getFullName())
				.email(employeeRequest.getEmail())
				.phone(employeeRequest.getPhone())
				.gender(employeeRequest.getGender())
				.dateOfBirth(employeeRequest.getDateOfBirth())
				.role(newRole)
				.address(employeeRequest.getAddress())
				.avatar(avatarPath)
				.build();

		userRepository.save(newUser);
		return true;
	}

	@Override
	public Page<UserDTO> searchUsers(String keyword, Boolean isActive, Pageable pageable) {
		Specification<User> spec = Specification
				.where(UserSpecification.containsKeyword(keyword))
				.and(UserSpecification.isActive(isActive))
				.and(UserSpecification.hasRoleIn(1, 2));

		Page<User> users = userRepository.findAll(spec, pageable);

		return users.map(entity -> {
			UserDTO dto = modelMapper.map(entity, UserDTO.class);

			if (entity.getRole() != null) {
				dto.setRoleId(entity.getRole().getId());
				dto.setRoleName(entity.getRole().getNameRole());
			}
			return dto;
		});

	}

	@Override
	public long countUsersByRoles(String keyword, Integer... roleIds) {
		Specification<User> spec = Specification.where(null);

		if (keyword != null && !keyword.trim().isEmpty()) {
			spec = spec.and(UserSpecification.containsKeyword(keyword));
		}

		spec = spec.and(UserSpecification.hasRoleIn(roleIds));

		return userRepository.count(spec);
	}

	public User fromRequest(EmployeeRequest req) {

		User.UserBuilder userBuilder = User.builder()
				.userName(req.getUserName())
				.fullName(req.getFullName())
				.email(req.getEmail())
				.phone(req.getPhone())
				.address(req.getAddress())
				.gender(req.getGender())
				.dateOfBirth(req.getDateOfBirth())
				.isActive(false); // default

		if (req.getPassword() != null && !req.getPassword().isEmpty()) {
			userBuilder.password(passwordEncoder.encode(req.getPassword()));
		}

		Role role = roleRepository.findById(req.getRoleId())
				.orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò với id: " + req.getRoleId()));
		userBuilder.role(role);

		String userCode = generateUserCode(role.getId());
		userBuilder.userCode(userCode);

		if (req.getAvatar() != null && !req.getAvatar().isEmpty()) {
			String avatarPath = uploadAvatar(req.getAvatar());
			userBuilder.avatar(avatarPath);
		}

		return userBuilder.build();
	}

	private String uploadAvatar(MultipartFile avatar) {
		try {
			return imageService.saveImage(avatar, "user");
		} catch (IOException e) {
			throw new RuntimeException("Lỗi khi lưu ảnh: " + e.getMessage(), e);
		}
	}

	private String generateUserCode(Integer roleId) {
		String prefix;
		switch (roleId) {
			case 1:
				prefix = "AD";
				break;
			case 2:
				prefix = "NV";
				break;
			case 3:
				prefix = "KH";
				break;
			default:
				prefix = "XX";
		}
		long count = userRepository.countByRoleId(roleId);
		return String.format("%s-%03d", prefix, count + 1);
	}

	private String handleUploadAvatar(MultipartFile avatar, String currentAvatarPath) {
		if (avatar != null && !avatar.isEmpty()) {
			try {
				if (currentAvatarPath != null && !currentAvatarPath.isEmpty()) {
					imageService.deleteImage(currentAvatarPath);
				}
				return imageService.saveImage(avatar, "user");
			} catch (IOException e) {
				throw new RuntimeException("Lỗi khi lưu ảnh: " + e.getMessage(), e);
			}
		}
		return currentAvatarPath;
	}
}
