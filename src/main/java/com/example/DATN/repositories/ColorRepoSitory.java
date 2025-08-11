package com.example.DATN.repositories;

import java.util.List;

import com.example.DATN.models.Color;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ColorRepoSitory extends JpaRepository<Color, Integer> {
	List<Color> findByNameContainingIgnoreCase(String name);

}
