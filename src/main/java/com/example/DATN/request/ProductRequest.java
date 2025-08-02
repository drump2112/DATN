package com.example.DATN.request;

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
public class ProductRequest {

	private Integer id;

	private String productCode;

	private String name;

	private String description;

	private Integer brandId;

	private Integer categoryId;

	private Date createAt;

	private Boolean isActive;

	private MultipartFile thumbnail;

}
