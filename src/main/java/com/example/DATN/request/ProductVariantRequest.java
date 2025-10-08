package com.example.DATN.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
	// private String sku;
	private Integer price;
	private Boolean status;
	private Integer productId;
	private Integer colorId;
	private List<Integer> sizeIds;
	private Map<Integer, Integer> quantities;
	private Map<Integer, String> skus;
	private List<MultipartFile> images;
}
