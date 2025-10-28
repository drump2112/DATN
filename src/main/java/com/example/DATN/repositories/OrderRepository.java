package com.example.DATN.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.DATN.models.Order;

public interface OrderRepository extends JpaRepository<Order, Integer> {

  Page<Order> findByOrderType(String orderType, Pageable pageable);

  long countByOrderDateBetween(LocalDateTime start, LocalDateTime end);

  Optional<Order> findByOrderCode(String orderCode);

  Page<Order> findByUserIdOrderByOrderDateDesc(Integer userId, Pageable pageable);
}
