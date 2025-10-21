package com.example.DATN.services;

import com.example.DATN.dtos.OrderDetailResponse;
import com.example.DATN.models.Order;
import com.example.DATN.request.CounterOrderRequest;

public interface OrderService {

  Order createCounterOrder(CounterOrderRequest request);

    OrderDetailResponse getOrderDetailByCode(String orderCode);
}
