package com.example.DATN.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// 🧩 1️⃣ SECURITY CHO ADMIN + SELLER
	@Bean
	@Order(1)
	public SecurityFilterChain adminSecurity(HttpSecurity http) throws Exception {
		http
				.securityMatcher("/admin/**", "/seller/**", "/login", "/do-login") // chỉ áp dụng filter này cho các URL
																					// này
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/assets/**", "/js/**", "/uploads/**").permitAll()
						.requestMatchers("/login", "/do-login").permitAll()
						.anyRequest().hasAnyRole("ADMIN", "SELLER"))
				.formLogin(form -> form
						.loginPage("/login")
						.loginProcessingUrl("/do-login")
						.usernameParameter("username")
						.passwordParameter("password")
						.successHandler(customSuccessHandler())
						.failureHandler(customAuthenticationFailureHandler)
						.permitAll())
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/login?logout"))
				.csrf().disable();

		return http.build();
	}

	// 🧩 2️⃣ SECURITY CHO KHÁCH HÀNG
	@Bean
	@Order(2)
	public SecurityFilterChain customerSecurity(HttpSecurity http) throws Exception {
		http
				.securityMatcher("/customer/**", "/", "/details/**", "/cart/**", "/customer/auth/**")
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(
								"/", "/register", "/verify/**",
								"/assets/**", "/js/**", "/uploads/**", "/details/**", "/cart/**",
								"/customer/auth/**")
						.permitAll()
						.anyRequest().hasRole("CUSTOMER"))
				.formLogin(form -> form
						.loginPage("/customer/auth/") // login page cho khách hàng
						.loginProcessingUrl("/customer/do-login")
						.usernameParameter("username")
						.passwordParameter("password")
						.successHandler(customSuccessHandler())
						.failureHandler(customAuthenticationFailureHandler)
						.permitAll())
				.logout(logout -> logout
						.logoutUrl("/customer/logout")
						.logoutSuccessUrl("/customer/auth/?logout"))
				.csrf().disable();

		return http.build();
	}

	@Bean
	public DaoAuthenticationProvider authProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder());
		return provider;
	}

	@Bean
	public AuthenticationSuccessHandler customSuccessHandler() {
		return (request, response, authentication) -> {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			String role = userDetails.getAuthorities().iterator().next().getAuthority();

			switch (role) {
				case "ROLE_ADMIN":
				case "ROLE_SELLER":
					response.sendRedirect("/admin/home");
					break;
				default:
					response.sendRedirect("/");
					break;
			}
		};
	}
}
