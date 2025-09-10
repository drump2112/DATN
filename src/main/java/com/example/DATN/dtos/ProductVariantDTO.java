package com.example.DATN.dtos;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ProductVariantDTO {

	private Integer id;
	private String variantCode;
	private String sku;
	private BigDecimal price;
	private Integer quantity;
	private Boolean status;

	private Integer productId;
	private String productName;

	private String productDescription;
	private Integer brandId;
	private String brandName;
	private Integer categoryId;

	private String categoryName;

	private Integer sizeId;
	private String sizeName;

	private Integer colorId;
	private String colorName;

	private List<String> imageUrls;

}
