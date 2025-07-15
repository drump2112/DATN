package com.example.DATN.request;

import java.time.LocalDate;
import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class EmployeeRequest {

	private Integer id;

	private String userCode;

	private String userName;

	private String email;

	private String password;

	private String fullName;

	private String address;

	private String phone;

	private Integer roleId;

	private String roleName;

	private Boolean isActive;

	private Boolean gender;

	private LocalDate dateOfBirth;

	private Date createAt;

	private MultipartFile avatar;
}
