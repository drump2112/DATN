package com.example.DATN.repositories;

import com.example.DATN.models.StockMovement;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<StockMovement, Integer> {

}
