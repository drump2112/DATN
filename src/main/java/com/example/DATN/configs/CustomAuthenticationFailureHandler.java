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
		String username = request.getParameter("username"); // lấy username từ form
		request.getSession().setAttribute("lastUsername", username);
		// Lưu lại redirectUrl nếu có
		String redirectUrl = request.getParameter("redirectUrl");
		String errorParam = "";

		if (exception instanceof DisabledException) {
			errorParam = "disabled";
		} else if (exception instanceof BadCredentialsException) {
			errorParam = "bad";
		} else {
			errorParam = "unknown";
		}

		String failureUrl = "/customer/auth/?error=" + errorParam;
		if (redirectUrl != null && !redirectUrl.isEmpty()) {
			failureUrl += "&redirectUrl=" + redirectUrl;
		}

		setDefaultFailureUrl(failureUrl);
		super.onAuthenticationFailure(request, response, exception);
	}
}
