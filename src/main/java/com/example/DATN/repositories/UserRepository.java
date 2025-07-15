package com.example.DATN.repositories;

import java.util.List;
import java.util.Optional;

import com.example.DATN.models.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {

	Page<User> findByRoleId(Integer roleId, Pageable pageable);

	Page<User> findByRoleIdNot(int roleId, Pageable pageable);

	Optional<User> findByUserName(String userName);

	long countByRoleId(Integer roleId);

	@EntityGraph(attributePaths = "role")
	Optional<User> findByUserNameOrEmail(String userName, String email);

	boolean existsByEmail(String email);
}
