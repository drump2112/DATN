package com.example.DATN.configs.email;

import com.example.DATN.models.Order;
import com.example.DATN.models.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	@Autowired
	JavaMailSender mailSender;

	public void sendVerificationEmail(User user, String token) {
		String subject = "Xác nhận tài khoản";
		String confirmationUrl = "http://localhost:8080/verify?token=" + token;
		String message = "Vui lòng nhấn vào link dưới để kích hoạt tài khoản:\n" + confirmationUrl;

		SimpleMailMessage email = new SimpleMailMessage();
		email.setTo(user.getEmail());
		email.setSubject(subject);
		email.setText(message);
		mailSender.send(email);
	}

	public void sendOtpEmail(String to, String otp) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);
		message.setSubject("Mã OTP xác nhận đơn hàng");
		message.setText("Mã OTP của bạn là: " + otp + "\nHết hạn sau 10 phút.");
		mailSender.send(message);
	}

	public void sendOrderConfirmation(Order order) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(order.getUser().getEmail());
		message.setSubject("Xác nhận đơn hàng " + order.getOrderCode());
		message.setText("Đơn hàng của bạn đã được xác nhận.\nTổng tiền: " + order.getFinalAmount());
		mailSender.send(message);
	}
}
