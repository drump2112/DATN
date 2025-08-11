package com.example.DATN.services;

import java.util.List;

import com.example.DATN.dtos.ProductVariantDTO;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.request.ProductVariantRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductVariantService {

	Page<ProductVariantDTO> getAllProducts(int page, int size);

	boolean toggleStatus(Integer id);

	boolean addProductVariant(ProductVariantRequest productVariantRequest);

	boolean updateProductVariant(Integer id);

	public Page<ProductVariantDTO> searchProductVariants(
			String keyword,
			Integer colorId,
			Integer sizeId,
			Integer cateId,
			Boolean status,
			Pageable pageable);

	public long countAll();

	public List<ProductVariantDTO> getVariantsByProductId(Integer productId);
}
