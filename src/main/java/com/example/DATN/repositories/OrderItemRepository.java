package com.example.DATN.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.DATN.models.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}
