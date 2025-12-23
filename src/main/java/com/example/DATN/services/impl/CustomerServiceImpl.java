package com.example.DATN.services.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.DATN.configs.email.EmailService;
import com.example.DATN.dtos.CustomerDTO;
import com.example.DATN.exception.BusinessException;
import com.example.DATN.models.Address;
import com.example.DATN.models.Role;
import com.example.DATN.models.User;
import com.example.DATN.models.VerificationToken;
import com.example.DATN.repositories.RoleRepository;
import com.example.DATN.repositories.UserRepository;
import com.example.DATN.repositories.VerificationTokenRepository;
import com.example.DATN.request.CustomerRequest;
import com.example.DATN.services.AddressService;
import com.example.DATN.services.CustomerService;
import com.example.DATN.services.ImageService;

@Service
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

	@Autowired
	private AddressService addressService;

	@Override
	public boolean addCustomer(CustomerRequest customerRequest) {
		List<Integer> checkRoles = List.of(1, 2, 3);

		if (userRepository.existsByEmailAndRoleIdIn(customerRequest.getEmail(), checkRoles)) {
			throw new BusinessException("Email đã được sử dụng.");
		}

		if (userRepository.existsByPhoneAndRoleIdIn(customerRequest.getPhone(), checkRoles)) {
			User existingCustomer = userRepository.findByPhoneAndRoleIdIn(customerRequest.getPhone(), checkRoles).get(0);
			updateCustomerFromRequest(existingCustomer, customerRequest);
			userRepository.save(existingCustomer);
			return true;
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

	@Override
	public boolean addQuickCustomer(String fullName, String phone) {
		List<Integer> checkRoles = List.of(1, 2);

		if (userRepository.existsByPhoneAndRoleIdIn(phone, checkRoles)) {
			throw new BusinessException("Số điện thoại đã được sử dụng.");
		}

		User customer = User.builder()
				.fullName(fullName)
				.phone(phone)
				.createAt(new Date())
				.isActive(true) // Khách hàng thêm nhanh mặc định active
				.role(roleRepository.findById(3).orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò khách hàng")))
				.userCode(generateUserCode(3))
				.build();

		userRepository.save(customer);
		return true;
	}

	public User fromRequest(CustomerRequest req) {

		User.UserBuilder userBuilder = User.builder()
				.userName(req.getUserName())
				.fullName(req.getFullName())
				.email(req.getEmail())
				.phone(req.getPhone())
				.address(createAddressFromRequest(req))
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

	private void updateCustomerFromRequest(User existingCustomer, CustomerRequest req) {
		existingCustomer.setUserName(req.getUserName());
		existingCustomer.setEmail(req.getEmail());
		if (req.getPassword() != null && !req.getPassword().isEmpty()) {
			existingCustomer.setPassword(passwordEncoder.encode(req.getPassword()));
		}
		existingCustomer.setAddress(createAddressFromRequest(req));
		existingCustomer.setGender(req.getGender());
		existingCustomer.setDateOfBirth(req.getDateOfBirth());
		existingCustomer.setIsActive(false); // Cần xác thực email

		if (req.getAvatar() != null && !req.getAvatar().isEmpty()) {
			String avatarPath = uploadAvatar(req.getAvatar());
			existingCustomer.setAvatar(avatarPath);
		}

		// Tạo token xác thực email
		String token = UUID.randomUUID().toString();
		VerificationToken verificationToken = new VerificationToken();
		verificationToken.setToken(token);
		verificationToken.setUser(existingCustomer);
		verificationToken.setExpiryDate(LocalDateTime.now().plusDays(1));

		tokenRepository.save(verificationToken);

		emailService.sendVerificationEmail(existingCustomer, token);
	}

	private Address createAddressFromRequest(CustomerRequest req) {
		if (req.getProvinceCode() == null || req.getCommuneCode() == null) {
			return null;
		}

		String specificAddress = req.getSpecificAddress() != null ? req.getSpecificAddress() : "";

		return addressService.createAddress(
				specificAddress,
				req.getCommuneCode(),
				req.getProvinceCode());
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

	@Override
	public List<CustomerDTO> getCustomers(String keyword) {
		List<User> customers;
		if (keyword != null && !keyword.isBlank()) {
			customers = userRepository.searchByNameOrPhone(keyword);
		} else {
			customers = userRepository.findByRole_Id(3);
		}

		return customers.stream()
				.map(customer -> CustomerDTO.builder()
						.id(customer.getId())
						.fullName(customer.getFullName())
						.email(customer.getEmail())
						.phone(customer.getPhone())
						.address(customer.getAddress() != null ? customer.getAddress().getFullAddress() : "")
						.gender(customer.isGender())
						.dateOfBirth(customer.getDateOfBirth())
						.build())
				.collect(Collectors.toList());
	}

}