package com.example.DATN.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
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
				// admin/seller pages
				.securityMatcher("/admin/**", "/seller/**", "/login", "/do-login", "/logout") // chỉ áp dụng filter này cho các
																																											// URL
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/assets/**", "/js/**", "/uploads/**").permitAll()
						.requestMatchers("/login", "/do-login").permitAll()
						.requestMatchers("/logout").permitAll()
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

	// SECURITY CHO KHÁCH HÀNG
	@Bean
	@Order(2)
	public SecurityFilterChain customerSecurity(HttpSecurity http) throws Exception {
		http
				.securityMatcher("/customer/**", "/", "/details/**", "/cart/**", "/checkout/**", "/orders/**",
						"/customer/auth/**", "/.well-known/**")
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(
								"/", "/register", "/verify/**",
								"/assets/**", "/js/**", "/uploads/**", "/details/**", "/cart/**",
								"/customer/auth/**", "/.well-known/**", "/customer/do-login")
						.permitAll()
						.requestMatchers("/checkout/**", "/orders/**").hasRole("CUSTOMER")
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
						.logoutSuccessUrl("/"))
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
			SecurityContextHolder.getContext().setAuthentication(authentication);
			request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			String role = userDetails.getAuthorities().iterator().next().getAuthority();

			// Lấy redirectUrl từ request parameter (từ URL)
			String redirectUrl = request.getParameter("redirectUrl");
			System.out.println("Success Handler - URL Parameter redirectUrl: " + redirectUrl);

			if (redirectUrl == null || redirectUrl.isEmpty()) {
				// Thử lấy từ session nếu không có trong parameter
				HttpSession session = request.getSession(false);
				if (session != null) {
					redirectUrl = (String) session.getAttribute("REDIRECT_URL");
					System.out.println("Success Handler - Session redirectUrl: " + redirectUrl);
					// Xóa khỏi session sau khi lấy
					session.removeAttribute("REDIRECT_URL");
				}
			}			System.out.println("Success Handler - Final redirectUrl: " + redirectUrl);			if ("ROLE_ADMIN".equals(role) || "ROLE_SELLER".equals(role)) {
				response.sendRedirect("/admin/home");
			} else if ("ROLE_CUSTOMER".equals(role)) {
				if (redirectUrl != null && !redirectUrl.isEmpty()) {
					response.sendRedirect(redirectUrl);
				} else {
					response.sendRedirect("/");
				}
			} else {
				response.sendRedirect("/");
			}
		};
	}
}