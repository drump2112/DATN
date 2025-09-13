package com.example.DATN;

import com.example.DATN.models.Role;
import com.example.DATN.models.User;
import com.example.DATN.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class DatnApplication {

    public static void main(String[] args) {
		SpringApplication.run(DatnApplication.class, args);
	}


}
