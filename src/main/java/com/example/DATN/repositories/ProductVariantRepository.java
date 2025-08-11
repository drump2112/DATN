package com.example.DATN.repositories;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.example.DATN.models.ProductVariant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductVariantRepository
		extends JpaRepository<ProductVariant, Integer>, JpaSpecificationExecutor<ProductVariant> {

	@EntityGraph(attributePaths = {
			"product",
			"product.brand",
			"product.category",
			"size",
			"color",
			"images"
	})
	Page<ProductVariant> findAll(Pageable pageable);

	List<ProductVariant> findByProductId(Integer productId);

	boolean existsByProductIdAndColorIdAndSizeId(Integer productId, Integer colorId, Integer sizeId);

	Optional<BigDecimal> findPriceByProductIdAndColorId(Integer productId, Integer colorId);
}
