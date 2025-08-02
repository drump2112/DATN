package com.example.DATN.repositories;

import java.util.List;
import java.util.Optional;

import com.example.DATN.models.Brand;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Integer> {

	Optional<Brand> findTopByOrderByIdDesc();

	List<Brand> findByNameContainingIgnoreCase(String name);
}
