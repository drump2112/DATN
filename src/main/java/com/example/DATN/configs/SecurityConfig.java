// package com.example.DATN.configs;
//
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
//
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.AuthenticationProvider;
// import
// org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//
// import
// org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
// import
// org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import
// org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
//
// import org.springframework.security.web.SecurityFilterChain;
//
// @Configuration
// @EnableWebSecurity
// public class SecurityConfig {
//
// @Bean
// public PasswordEncoder passwordEncoder() {
// return new BCryptPasswordEncoder();
// }
//
// @Bean
// public UserDetailsService userDetailsService() {
// return new CustomUserDetailsService();
// }
//
// @Bean
// public AuthenticationProvider authenticationProvider() {
// DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
// provider.setUserDetailsService(userDetailsService());
// provider.setPasswordEncoder(passwordEncoder());
// return provider;
// }
//
// @Bean
// public AuthenticationManager
// authenticationManager(AuthenticationConfiguration config) throws Exception {
// return config.getAuthenticationManager();
// }
//
// @Bean
// public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
// http
// .csrf(csrf -> csrf.disable())
// .authorizeHttpRequests(auth -> auth
// .requestMatchers("/admin/**") // .hasRole("ADMIN")
// .requestMatchers("/seller/**")// .hasRole("SELLER")
// .requestMatchers("/user/**")// .hasRole("CUSTOMER")
// .requestMatchers("/", "/login", "/assets/**").permitAll()
// .anyRequest().authenticated())
// .formLogin(form -> form
// .loginPage("/login") // nếu có custom login, nếu không có thì xóa dòng này
// .permitAll())
// .logout(logout -> logout
// .logoutUrl("/logout")
// .logoutSuccessUrl("/")
// .permitAll());
//
// return http.build();
// }
// }
