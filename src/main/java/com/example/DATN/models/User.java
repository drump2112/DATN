package com.example.DATN.models;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "Users")
@Builder
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "UserCode", nullable = false, unique = true, length = 10)
	private String userCode;

	@Column(name = "UserName", nullable = false, unique = true, length = 100)
	private String userName;

	@Column(name = "Password", nullable = false, unique = true, length = 100)
	private String password;

	@Column(name = "Email", length = 100)
	private String email;

	@Column(name = "FullName", length = 100)
	private String fullName;

	@Column(name = "Address", columnDefinition = "nvarchar(max)")
	private String address;

	@Column(name = "Phone", length = 10)
	private String phone;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "RoleID")
	private Role role;

	@Column(name = "CreateAt")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createAt;

	@Column(name = "isActive")
	private Boolean isActive;
}
