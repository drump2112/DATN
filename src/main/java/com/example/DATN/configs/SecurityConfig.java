package com.example.DATN.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf().disable()
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/", "/login", "/register", "/verify/**", "/assets/**",
								"/js/**")
						.permitAll()
						.requestMatchers("/admin/**").hasRole("ADMIN")
						.requestMatchers("/seller/**").hasRole("SELLER")
						.requestMatchers("/customer/**").hasRole("CUSTOMER")
						.anyRequest().authenticated())
				.formLogin(form -> form
						.loginPage("/login")
						.defaultSuccessUrl("/home", true)
						.loginProcessingUrl("/do-login")
						.usernameParameter("username")
						.passwordParameter("password")
						.successHandler(customSuccessHandler())
						.failureHandler(customAuthenticationFailureHandler)
						.permitAll())
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/login?logout")
						.permitAll())
				.exceptionHandling()
				.authenticationEntryPoint((request, response, authException) -> {
					String ajaxHeader = request.getHeader("X-Requested-With");
					if ("XMLHttpRequest".equals(ajaxHeader)) {
						response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session expired");
					} else {
						response.sendRedirect("/login");
					}
				});

		return http.build();
	}

	@Bean
	public AuthenticationSuccessHandler customSuccessHandler() {
		return (request, response, authentication) -> {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			String role = userDetails.getAuthorities().iterator().next().getAuthority();

			switch (role) {
				case "ROLE_ADMIN":
					response.sendRedirect("/home");
					break;
				case "ROLE_SELLER":
					response.sendRedirect("/home");
					break;
				default:
					response.sendRedirect("/customer/home");
					break;
			}
		};
	}

	@Bean
	public AuthenticationFailureHandler customFailureHandler() {
		return (request, response, exception) -> {
			String message = "Đăng nhập thất bại!";
			if (exception.getMessage().contains("disabled")) {
				message = "Tài khoản chưa được kích hoạt!";
			}
			request.getSession().setAttribute("error", message);
			response.sendRedirect("/login?error");
		};
	}

	@Bean
	public DaoAuthenticationProvider authProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}
}
