package com.example.DATN.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Sizes")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Size {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "SizeCode", unique = true, length = 10, nullable = false)
	private String sizeCode;

	@Column(name = "Name", nullable = false, length = 100)
	private String name;

}
