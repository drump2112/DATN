package com.example.DATN.repositories;

import com.example.DATN.models.SaleEventProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleEventProductRepository extends JpaRepository<SaleEventProduct, Integer> {
    List<SaleEventProduct> findBySalesEventId(Integer salesEventId);
    void deleteBySalesEventId(Integer salesEventId);
}