package com.example.DATN.repositories;

import java.util.List;
import java.util.Optional;

import com.example.DATN.dtos.ProductDTO;
import com.example.DATN.models.Product;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {

	Optional<Product> findTopByOrderByIdDesc();

	boolean existsByProductCode(String productCode);

	boolean existsByName(String name);

	Optional<Product> findById(Integer id);

	List<Product> findByNameContainingIgnoreCase(String name);

	@EntityGraph(attributePaths = { "variants" })
	List<Product> findByIsActive(boolean isActive);

}
