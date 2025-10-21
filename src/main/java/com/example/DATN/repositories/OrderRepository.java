package com.example.DATN.repositories;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.DATN.models.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

  long countByOrderDateBetween(LocalDateTime start, LocalDateTime end);

   Optional<Order> findByOrderCode(String orderCode);
}
