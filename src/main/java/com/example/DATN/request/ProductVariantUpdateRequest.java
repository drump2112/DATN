package com.example.DATN.request;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import lombok.Data;

@Data
public class ProductVariantUpdateRequest {
	private Integer price;
	private Integer quantity;
	private List<MultipartFile> images;
}
