package com.example.DATN.services.impl;

import com.example.DATN.dtos.OrderDetailResponse;
import com.example.DATN.dtos.OrderItemDTO;
import com.example.DATN.dtos.OrderItemResponse;
import com.example.DATN.models.Order;
import com.example.DATN.models.OrderItem;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.models.User;
import com.example.DATN.repositories.OrderRepository;
import com.example.DATN.repositories.ProductVariantRepository;
import com.example.DATN.repositories.UserRepository;
import com.example.DATN.repositories.VoucherRepository;
import com.example.DATN.request.CounterOrderRequest;
import com.example.DATN.services.OrderService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Override
    @Transactional
    public Order createCounterOrder(CounterOrderRequest request) {
        BigDecimal totalAmount = BigDecimal.ZERO;

        // Kiểm tra tồn kho trước khi tạo đơn
        for (OrderItemDTO dto : request.getItems()) {
            ProductVariant pv = productVariantRepository.findById(Integer.valueOf(dto.getProductVariantId()))
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

            if (dto.getQuantity() > pv.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + pv.getProduct().getName() + " vượt quá tồn kho!");
            }
        }

        // Tạo Order
        Order order = new Order();
        order.setOrderType("COUNTER");
        order.setStatus("COMPLETED");
        String code = generateOrderCode(order.getOrderType());
        order.setOrderCode(code);

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
            order.setUser(user);
        }

        order.setPaymentMethod(request.getPaymentMethod());

        List<OrderItem> items = new ArrayList<>();

        for (OrderItemDTO dto : request.getItems()) {
            ProductVariant pv = productVariantRepository.findById(Integer.valueOf(dto.getProductVariantId())).get();

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductVariant(pv);
            item.setQuantity(dto.getQuantity());
            item.setUnitPrice(dto.getUnitPrice());

            totalAmount = totalAmount.add(dto.getUnitPrice().multiply(new BigDecimal(dto.getQuantity())));

            // Trừ tồn kho
            pv.setQuantity(pv.getQuantity() - dto.getQuantity());
            productVariantRepository.save(pv);

            items.add(item);
        }

        BigDecimal discountAmount = BigDecimal.ZERO;

        if (request.getVoucherId() != null) {
            order.setDiscountAmount(request.getDiscountAmount());
            discountAmount = request.getDiscountAmount();
            order.setVoucher(voucherRepository.findById(request.getVoucherId()).orElse(null));
        } else {
            order.setDiscountAmount(BigDecimal.ZERO);
            order.setVoucher(null);

        }

        order.setItems(items);
        order.setTotalAmount(totalAmount);
        order.setFinalAmount(totalAmount.subtract(discountAmount)); // chưa tính giảm giá
        return orderRepository.save(order);
    }

    private String generateOrderCode(String orderType) {
        String prefix = orderType.equals("COUNTER") ? "POS-" : "ONL-";

        // Lấy ngày hiện tại
        LocalDate today = LocalDate.now();
        String datePart = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Lấy số thứ tự trong ngày (tùy cơ chế)
        int count = (int) (orderRepository.countByOrderDateBetween(
                today.atStartOfDay(), today.plusDays(1).atStartOfDay()) + 1);

        String orderCode = String.format("%s%s-%04d", prefix, datePart, count);
        return orderCode;
    }

    @Override
    public OrderDetailResponse getOrderDetailByCode(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn hàng có mã: " + orderCode));

        return OrderDetailResponse.builder()
                .orderCode(order.getOrderCode())
                .orderDate(order.getOrderDate())
                .orderType(order.getOrderType())
                .status(order.getStatus())
                .customerName(order.getUser() != null ? order.getUser().getFullName() : "Khách lẻ")
                .customerPhone(order.getUser() != null ? order.getUser().getPhone() : "")
                .shippingAddress(order.getShippingAddress())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .paymentMethod(order.getPaymentMethod())
                .items(mapItems(order.getItems()))
                .build();
    }

    private List<OrderItemResponse> mapItems(List<OrderItem> items) {
        return items.stream().map(i -> OrderItemResponse.builder()
                .variantCode(i.getProductVariant().getVariantCode())
                .productName(i.getProductVariant().getProduct().getName())
                .color(i.getProductVariant().getColor().getName())
                .size(i.getProductVariant().getSize().getName())
                .unitPrice(i.getUnitPrice())
                .quantity(i.getQuantity())
                .totalPrice(i.getUnitPrice().multiply(
                        java.math.BigDecimal.valueOf(i.getQuantity())))
                .build()).toList();
    }
}
