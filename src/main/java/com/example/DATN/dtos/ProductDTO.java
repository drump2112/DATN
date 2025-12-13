package com.example.DATN.dtos;

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
public class ProductDTO {

	private Integer id;

	private String productCode;

	private String name;

	private String description;

	private Integer brandId;

	private String brandName;

	private Integer categoryId;

	private String categoryName;

	private Date createAt;

	private Boolean isActive;

	private String thumbnail;

	private Double minPrice;

	private Double maxPrice;

	private Integer totalQuantity;
}
