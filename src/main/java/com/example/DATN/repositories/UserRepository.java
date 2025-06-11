package com.example.DATN.repositories;

import java.util.List;
import java.util.Optional;

import com.example.DATN.models.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

	Page<User> findByRoleId(Integer roleId, Pageable pageable);

	Page<User> findByRoleIdNot(int roleId, Pageable pageable);

	Optional<User> findByUserName(String userName);
}
