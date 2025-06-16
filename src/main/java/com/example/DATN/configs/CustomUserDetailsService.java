package com.example.DATN.configs;

import java.util.Optional;

import com.example.DATN.models.User;
import com.example.DATN.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
		Optional<User> userOpt = userRepository.findByUserNameOrEmail(input, input);
		if (!userOpt.isPresent()) {
			throw new UsernameNotFoundException("Tài khoản không tồn tại");
		}

		User user = userOpt.get();
		System.out.println(user.toString());

		return new CustomUserDetails(user); // chứa logic phân quyền
	}
}
