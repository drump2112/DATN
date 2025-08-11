package com.example.DATN.repositories;

import java.util.List;

import com.example.DATN.models.Size;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SizeRepository extends JpaRepository<Size, Integer> {
	List<Size> findByNameContainingIgnoreCase(String name);

}
