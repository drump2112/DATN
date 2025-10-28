package com.example.DATN.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.DATN.models.OrderItem;
import com.example.DATN.repositories.OrderItemRepository;

@Service
public class OrderItemService {

  @Autowired
  private OrderItemRepository orderItemRepository;

  public List<OrderItem> getItemsByOrderId(Integer orderId) {
    return orderItemRepository.findByOrderId(orderId);
  }
}
