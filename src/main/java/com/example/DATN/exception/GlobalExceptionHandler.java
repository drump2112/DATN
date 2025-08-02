package com.example.DATN.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<?> handleBusiness(BusinessException e) {
		return ResponseEntity
				.badRequest()
				.body(Map.of("message", e.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleUnexpected(Exception e) {
		e.printStackTrace();
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("message", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau."));
	}
}
