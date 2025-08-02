package com.example.DATN.configs;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
	@Override
	public void onAuthenticationFailure(HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		if (exception instanceof DisabledException) {
			setDefaultFailureUrl("/login?error=disabled");
		} else if (exception instanceof BadCredentialsException) {
			setDefaultFailureUrl("/login?error=bad");
		} else {
			setDefaultFailureUrl("/login?error=unknown");
		}
		super.onAuthenticationFailure(request, response, exception);
	}
}
