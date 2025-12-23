package com.example.DATN.specifications;

import java.util.ArrayList;
import java.util.List;

import com.example.DATN.models.Product;
import com.example.DATN.models.ProductVariant;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

public class ProductVariantSpecification {

	public static Specification<ProductVariant> containsKeyword(String keyword) {
		return (root, query, cb) -> {
			if (keyword == null || keyword.trim().isEmpty())
				return null;

			String like = "%" + keyword.trim().toLowerCase() + "%";

			var productJoin = root.join("product", JoinType.LEFT);
			var brandJoin = productJoin.join("brand", JoinType.LEFT);

			return cb.or(
					cb.like(cb.lower(productJoin.get("name")), like),
					cb.like(cb.lower(brandJoin.get("name")), like),
					cb.like(cb.lower(root.get("variantCode")), like));
		};
	}

	public static Specification<ProductVariant> hasColor(Integer colorId) {
		return (root, query, cb) -> {
			if (colorId == null)
				return null;
			return cb.equal(root.get("color").get("id"), colorId);
		};
	}

	public static Specification<ProductVariant> hasSize(Integer sizeId) {
		return (root, query, cb) -> {
			if (sizeId == null)
				return null;
			return cb.equal(root.get("size").get("id"), sizeId);
		};
	}

	public static Specification<ProductVariant> hasCategory(Integer cateId) {
		return (root, query, cb) -> {
			if (cateId == null)
				return null;
			return cb.equal(root.get("product").get("category").get("id"), cateId);
		};
	}

	public static Specification<ProductVariant> hasBrand(Integer brandId) {
		return (root, query, cb) -> {
			if (brandId == null)
				return null;
			return cb.equal(root.get("product").get("brand").get("id"), brandId);

		};
	}

	public static Specification<ProductVariant> hasStatus(Boolean status) {
		return (root, query, cb) -> {
			if (status == null)
				return null;
			return cb.equal(root.get("status"), status);
		};
	}
}
