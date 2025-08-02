package com.example.DATN.repositories;

import java.util.Optional;

import com.example.DATN.models.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {

	Optional<Product> findTopByOrderByIdDesc();

	boolean existsByProductCode(String productCode);
}
