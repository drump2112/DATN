package com.example.DATN.dtos;

import java.time.LocalDate;
import java.util.Date;

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
public class UserDTO {

	private Integer id;

	private String userCode;

	private String userName;

	private String email;

	private String password;

	private String fullName;

	private String address;

	private String fullAddress;

	private String phone;

	private Integer roleId;

	private String roleName;

	private Boolean isActive;

	private Boolean gender;

	private LocalDate dateOfBirth;

	private Date createAt;

	private String avatar;
}
