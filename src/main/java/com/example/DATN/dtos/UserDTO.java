package com.example.DATN.dtos;

import java.util.Date;

import com.example.DATN.models.Role;

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

	private String fullName;

	private String address;

	private String phone;

	private Integer roleId;

	private String roleName;

	private Boolean isActive;

	private Date createAt;

}
