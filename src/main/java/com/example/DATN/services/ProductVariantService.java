package com.example.DATN.services;

import java.util.List;

import com.example.DATN.dtos.ProductVariantDTO;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.request.ProductVariantRequest;
import com.example.DATN.request.ProductVariantUpdateRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductVariantService {

	Page<ProductVariantDTO> getAllProducts(int page, int size);

	List<ProductVariantDTO> search(String keyword);

	boolean toggleStatus(Integer id);

	boolean addProductVariant(ProductVariantRequest productVariantRequest);

	boolean updateProductVariant(Integer id);

	public Page<ProductVariantDTO> searchProductVariants(
			String keyword,
			Integer colorId,
			Integer sizeId,
			Integer cateId,
			Integer brandId,
			Boolean status,
			Pageable pageable);

	public Page<ProductVariantDTO> searchProductVariantsInventory(
			String keyword,
			Integer colorId,
			Integer sizeId,
			Integer cateId,
			Pageable pageable);



	public long countAll();

	public void updateProductVariant(Integer id, ProductVariantUpdateRequest request);

	public ProductVariantDTO findById(Integer id);

	public List<ProductVariantDTO> getVariantsByProductId(Integer productId);
}
