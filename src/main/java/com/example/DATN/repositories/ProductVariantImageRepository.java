package com.example.DATN.repositories;

import java.util.List;

import com.example.DATN.models.ProductVariantImage;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVariantImageRepository extends JpaRepository<ProductVariantImage, Integer> {
	List<ProductVariantImage> findByProductIdAndColorIdOrderBySortOrder(Integer productId, Integer colorId);
}
