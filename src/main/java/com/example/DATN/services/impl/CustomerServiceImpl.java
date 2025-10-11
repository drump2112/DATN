package com.example.DATN.services.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import com.example.DATN.configs.email.EmailService;
import com.example.DATN.exception.BusinessException;
import com.example.DATN.models.Role;
import com.example.DATN.models.User;
import com.example.DATN.models.VerificationToken;
import com.example.DATN.repositories.RoleRepository;
import com.example.DATN.repositories.UserRepository;
import com.example.DATN.repositories.VerificationTokenRepository;
import com.example.DATN.request.CustomerRequest;
import com.example.DATN.request.EmployeeRequest;
import com.example.DATN.services.CustomerService;
import com.example.DATN.services.ImageService;

public class CustomerServiceImpl implements CustomerService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private VerificationTokenRepository tokenRepository;

  @Autowired
  private EmailService emailService;

  @Autowired
  private ImageService imageService;

  @Autowired
  private RoleRepository roleRepository;

  @Override
  public boolean addCustomer(CustomerRequest customerRequest) {
    List<Integer> checkRoles = List.of(1, 2);

    if (userRepository.existsByEmailAndRoleIdIn(customerRequest.getEmail(), checkRoles)) {
      throw new BusinessException("Email đã được sử dụng.");
    }

    if (userRepository.existsByPhoneAndRoleIdIn(customerRequest.getPhone(), checkRoles)) {
      throw new BusinessException("Số điện thoại đã được sử dụng.");
    }

    if (userRepository.existsByUserNameAndRoleIdIn(customerRequest.getUserName(), checkRoles)) {
      throw new BusinessException("Tên đăng nhập đã tồn tại");
    }

    User customer = fromRequest(customerRequest);

    userRepository.save(customer);

    String token = UUID.randomUUID().toString();
    VerificationToken verificationToken = new VerificationToken();
    verificationToken.setToken(token);
    verificationToken.setUser(customer);
    verificationToken.setExpiryDate(LocalDateTime.now().plusDays(1));

    tokenRepository.save(verificationToken);

    emailService.sendVerificationEmail(customer, token);

    return true;
  };

  public User fromRequest(CustomerRequest req) {

		User.UserBuilder userBuilder = User.builder()
				.userName(req.getUserName())
				.fullName(req.getFullName())
				.email(req.getEmail())
				.phone(req.getPhone())
				.address(req.getAddress())
				.gender(req.getGender())
				.dateOfBirth(req.getDateOfBirth())
				.createAt(new Date())
				.isActive(false); // default

		if (req.getPassword() != null && !req.getPassword().isEmpty()) {
			userBuilder.password(passwordEncoder.encode(req.getPassword()));
		}

		Role role = roleRepository.findById(3)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò với id: " + req.getRoleId()));
		userBuilder.role(role);

		String userCode = generateUserCode(3);
		userBuilder.userCode(userCode);

		if (req.getAvatar() != null && !req.getAvatar().isEmpty()) {
			String avatarPath = uploadAvatar(req.getAvatar());
			userBuilder.avatar(avatarPath);
		}

		return userBuilder.build();
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
		String userCode;
		int suffix = (int) count + 1;

		// Tăng suffix cho đến khi tìm được UserCode duy nhất
		do {
			userCode = String.format("%s-%03d", prefix, suffix);
			suffix++;
		} while (userRepository.existsByUserCode(userCode));

		return userCode;
	}

  	private String uploadAvatar(MultipartFile avatar) {
		try {
			return imageService.saveImage(avatar, "customer");
		} catch (IOException e) {
			throw new RuntimeException("Lỗi khi lưu ảnh: " + e.getMessage(), e);
		}
	}
}
