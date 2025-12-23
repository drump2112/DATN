package com.example.DATN.specifications;

import com.example.DATN.models.User;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.JoinType;

public class UserSpecification {
	public static Specification<User> containsKeyword(String keyword) {
		return (root, query, cb) -> {
			if (keyword == null || keyword.trim().isEmpty())
				return null;
			String like = "%" + keyword.trim() + "%";
			var roleJoin = root.join("role", JoinType.LEFT);
			return cb.or(
					cb.like(root.get("userCode"), like),
					cb.like(root.get("fullName"), like),
					cb.like(root.get("email"), like),
					cb.like(root.get("phone"), like),
					cb.like(root.get("userName"), like),
					cb.like(cb.lower(roleJoin.get("nameRole")), like));
		};
	}

	public static Specification<User> isActive(Boolean active) {
		return (root, query, cb) -> {
			if (active == null)
				return null;
			return cb.equal(root.get("isActive"), active);
		};
	}

	public static Specification<User> hasRoleIn(Integer... roleIds) {
		return (root, query, cb) -> {
			if (roleIds == null || roleIds.length == 0)
				return null;
			return root.get("role").get("id").in((Object[]) roleIds);
		};
	}
}
