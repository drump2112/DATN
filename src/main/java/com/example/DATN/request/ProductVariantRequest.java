package com.example.DATN.request;

import java.math.BigDecimal;

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
public class ProductVariantRequest {
	private String variantCode;
	private String sku;
	private BigDecimal price;
	private Integer quantity;
	private Boolean status;
	private Integer productId;
	private Integer sizeId;
	private Integer colorId;
	private MultipartFile[] images;
}
