package com.example.DATN.specifications;

import com.example.DATN.models.Product;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.JoinType;

public class ProductSpecification {
	public static Specification<Product> containsKeyword(String keyword) {
		return (root, query, cb) -> {
			if (keyword == null || keyword.trim().isEmpty())
				return null;
			String like = "%" + keyword.trim().toLowerCase() + "%";
			var brandJoin = root.join("brand", JoinType.LEFT);
			var categoryJoin = root.join("category", JoinType.LEFT);
			return cb.or(
					cb.like(root.get("productCode"), like),
					cb.like(root.get("name"), like),
					cb.like(root.get("description"), like),
					cb.like(cb.lower(brandJoin.get("name")), like),
					cb.like(cb.lower(categoryJoin.get("name")), like));
		};
	}

	public static Specification<Product> isActive(Boolean active) {
		return (root, query, cb) -> {
			if (active == null)
				return null;
			return cb.equal(root.get("isActive"), active);
		};
	}

}
