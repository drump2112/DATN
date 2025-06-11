// package com.example.DATN.configs;
//
// import com.example.DATN.models.User;
// import com.example.DATN.repositories.UserRepository;
//
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import
// org.springframework.security.core.userdetails.UsernameNotFoundException;
//
// public class CustomUserDetailsService implements UserDetailsService {
//
// @Autowired
// UserRepository userRepository;
//
// @Override
// public UserDetails loadUserByUsername(String username) throws
// UsernameNotFoundException {
// User user = userRepository.findByUserName(username)
// .orElseThrow(() -> new UsernameNotFoundException("User not found: " +
// username));
// return new CustomUserDetails(user);
// }
// }
