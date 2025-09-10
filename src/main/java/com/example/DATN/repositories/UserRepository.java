package com.example.DATN.repositories;

import java.util.List;
import java.util.Optional;

import com.example.DATN.models.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {

	Page<User> findByRoleId(Integer roleId, Pageable pageable);

	Page<User> findByRoleIdNot(int roleId, Pageable pageable);

	Optional<User> findByUserName(String userName);

	Optional<User> findById(Integer id);

	long countByRoleId(Integer roleId);

	boolean existsByUserCode(String userCode);

	@EntityGraph(attributePaths = "role")
	Optional<User> findByUserNameOrEmail(String userName, String email);

	@Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
			"FROM User u WHERE u.email = :email AND u.role.id IN :roleIds")
	boolean existsByEmailAndRoleIdIn(@Param("email") String email,
			@Param("roleIds") List<Integer> roleIds);

	@Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
			"FROM User u WHERE u.phone = :phone AND u.role.id IN :roleIds")
	boolean existsByPhoneAndRoleIdIn(@Param("phone") String phone,
			@Param("roleIds") List<Integer> roleIds);

	@Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
			"FROM User u WHERE u.userName = :userName AND u.role IS NOT NULL AND u.role.id IN :roleIds")
	boolean existsByUserNameAndRoleIdIn(@Param("userName") String userName,
			@Param("roleIds") List<Integer> roleIds);

	boolean existsByEmail(String email);
}
