package com.example.DATN.services.impl;

import com.example.DATN.Util.OTPUtil;
import com.example.DATN.configs.email.EmailService;
import com.example.DATN.dtos.CartItemDTO;
import com.example.DATN.dtos.OrderDTO;
import com.example.DATN.dtos.OrderDetailResponse;
import com.example.DATN.dtos.OrderItemDTO;
import com.example.DATN.dtos.OrderItemResponse;
import com.example.DATN.models.Order;
import com.example.DATN.models.OrderItem;
import com.example.DATN.models.Product;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.models.User;
import com.example.DATN.models.Voucher;
import com.example.DATN.repositories.OrderItemRepository;
import com.example.DATN.repositories.OrderRepository;
import com.example.DATN.repositories.ProductVariantRepository;
import com.example.DATN.repositories.UserRepository;
import com.example.DATN.repositories.VoucherRepository;
import com.example.DATN.request.CounterOrderRequest;
import com.example.DATN.request.OrderRequest;
import com.example.DATN.services.OrderService;
import com.example.DATN.services.RedisOtpService;
import com.example.DATN.services.StockMovementService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Email;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private StockMovementService stockMovementService;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private RedisOtpService redisOtpService;

    @Autowired
    private EmailService emailService;

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

        Order order = new Order();
        order.setOrderType("COUNTER");
        order.setStatus("COMPLETED");
        String code = generateOrderCode(order.getOrderType());
        order.setOrderCode(code);

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
            order.setUser(user);

            order.setCustomerName(user.getFullName());
        } else {
            order.setCustomerName("Khách Lẻ");
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

        Order savedOrder = orderRepository.save(order);

        // Ghi lại stock movement cho đơn hàng tại quầy
        processOrderItems(savedOrder);

        return savedOrder;
    }

    @Override
    @Transactional
    public Order createOrder(OrderRequest dto) {
        User user = userRepository.findById(dto.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));
        Order order = new Order();

        Voucher voucher = null;
        BigDecimal discountAmount = BigDecimal.ZERO;

        if (dto.getVoucherCode() != null && !dto.getVoucherCode().isEmpty()) {
            discountAmount = dto.getDiscountAmount();
            voucher = voucherRepository.findByCode(dto.getVoucherCode());
            order.setVoucher(voucher);
        } else {
            order.setDiscountAmount(BigDecimal.ZERO);
            order.setVoucher(null);
        }

        order.setUser(user);
        order.setCustomerName(dto.getCustomerName());
        order.setShippingAddress(dto.getShippingAddress());
        order.setShippingPhone(dto.getShippingPhone());
        order.setShippingFee(dto.getShippingFee());
        order.setOrderDate(LocalDateTime.now());
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setTotalAmount(dto.getTotalAmount());
        order.setDiscountAmount(dto.getDiscountAmount());
        order.setFinalAmount(dto.getFinalAmount());

        order.setOrderType("ONLINE");
        order.setOrderCode(generateOrderCode("ONLINE"));

        if ("CASH".equalsIgnoreCase(dto.getPaymentMethod())) {
            order.setStatus("WAITING_OTP");
        } else {
            order.setStatus("PENDING");
        }
        order = orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItemDTO item : dto.getItems()) {
            ProductVariant variant = productVariantRepository.findById(item.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found: " + item.getVariantId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductVariant(variant);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(BigDecimal.valueOf(item.getPrice()));
            orderItemRepository.save(orderItem);
            orderItems.add(orderItem);
        }

        order.setItems(orderItems);

        if ("CASH".equalsIgnoreCase(dto.getPaymentMethod())) {
            String otp = OTPUtil.generateOTP(6);
            redisOtpService.saveOtp(user.getEmail(), otp, order.getId());
            emailService.sendOtpEmail(user.getEmail(), otp);
        }
        return order;
    }

    @Transactional
    public boolean confirmOtp(Integer orderId, String email, String inputOtp) {
        String storedOtp = redisOtpService.getOtp(email, orderId);
        if (storedOtp == null || !storedOtp.equals(inputOtp)) {
            return false;
        }

        redisOtpService.deleteOtp(email, orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!"WAITING_OTP".equalsIgnoreCase(order.getStatus())) {
            throw new RuntimeException("Đơn hàng không ở trạng thái chờ OTP");
        }

        // Kiểm tra tồn kho trước khi trừ
        for (OrderItem item : order.getItems()) {
            ProductVariant variant = productVariantRepository.findByIdForUpdate(item.getProductVariant().getId());

            if (variant == null) {
                throw new RuntimeException("Không tìm thấy biến thể sản phẩm");
            }

            if (variant.getQuantity() < item.getQuantity()) {
                throw new RuntimeException("Biến thể " + variant.getVariantCode() + " không đủ hàng");
            }
        }

        order.setStatus("PENDING");
        orderRepository.save(order);

        // Ghi lại stock movement cho đơn hàng online sau khi confirm OTP
        processOrderItems(order);

        emailService.sendOrderConfirmation(order);

        return true;
    }

    private String generateOrderCode(String orderType) {
        String prefix = orderType.equals("COUNTER") ? "POS-" : "ONL-";

        LocalDate today = LocalDate.now();
        String datePart = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        int count = (int) (orderRepository.countByOrderDateBetween(
                today.atStartOfDay(), today.plusDays(1).atStartOfDay()) + 1);

        String orderCode = String.format("%s%s-%04d", prefix, datePart, count);
        return orderCode;
    }

    @Override
    public OrderDetailResponse getOrderDetailByCode(String orderCode) {
        Order order = orderRepository.findByOrderCodeWithItems(orderCode)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn hàng có mã: " + orderCode));

        System.out.println("=== Order found in service ===");
        System.out.println("Order code: " + order.getOrderCode());
        System.out.println("Items size: " + (order.getItems() != null ? order.getItems().size() : "null"));

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

    @Override
    public Page<OrderDTO> getOnlineOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findByOrderType("ONLINE", pageable);
        return orders.map(this::mapToOrderDTO);
    }

    @Override
    public Page<OrderDTO> getOfflineOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findByOrderType("COUNTER", pageable);
        return orders.map(this::mapToOrderDTO);
    }

    @Override
    public Page<OrderDTO> getUserOrders(Integer userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findByUserIdOrderByOrderDateDesc(userId, pageable);
        return orders.map(this::mapToOrderDTO);
    }

    private OrderDTO mapToOrderDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderCode(order.getOrderCode());
        dto.setCustomerName(order.getCustomerName());
        dto.setUserCode(order.getUser() != null ? order.getUser().getUserCode() : "");
        dto.setShippingPhone(order.getShippingPhone());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setDiscountAmount(order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO);
        dto.setFinalAmount(order.getFinalAmount());
        dto.setShippingFee(order.getShippingFee());
        dto.setOrderType(order.getOrderType());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setStatus(order.getStatus());
        if (order.getVoucher() != null) {
            dto.setVoucherCode(order.getVoucher().getCode());
        }
        return dto;
    }

    @Override
    public OrderDTO getOrderById(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        return mapToOrderDTO(order);
    }

    @Override
    @Transactional
    public boolean updateOrderStatus(Integer orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!status.matches("PENDING|PROCESSING|SHIPPING|COMPLETED|CANCELLED|RETURN")) {
            throw new RuntimeException("Trạng thái không hợp lệ");
        }

        order.setStatus(status);
        orderRepository.save(order);
        return true;
    }

    @Override
    public Order findById(Integer orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    @Override
    @Transactional
    public void updatePaymentStatus(Order order, String status, String transactionNo) {
        String oldStatus = order.getStatus();
        order.setStatus(status);
        order.setPaymentStatus("PAID");
        order.setTransactionNo(transactionNo);
        orderRepository.save(order);

        // Nếu đơn hàng chuyển từ PENDING sang COMPLETED và chưa trừ kho, thì trừ kho
        if ("PENDING".equals(oldStatus) && "COMPLETED".equals(status)) {
            processOrderItems(order);
        }
    }

    @Override
    @Transactional
    public void processOrderItems(Order order) {
        System.out.println("🔄 Processing order items for order: " + order.getId());

        // Xử lý voucher - trừ số lượng voucher nếu có
        if (order.getVoucher() != null) {
            Voucher voucher = order.getVoucher();
            System.out.println("🎫 Processing voucher: " + voucher.getCode() + ", Current quantity: " + voucher.getQuantity());

            if (voucher.getQuantity() != null && voucher.getQuantity() > 0) {
                // Kiểm tra xem voucher đã được sử dụng chưa (tránh trừ 2 lần)
                voucher.setQuantity(voucher.getQuantity() - 1);
                voucherRepository.save(voucher);
                System.out.println("✅ Voucher quantity decreased. New quantity: " + voucher.getQuantity());
            } else if (voucher.getQuantity() != null && voucher.getQuantity() == 0) {
                System.out.println("⚠️ Warning: Voucher quantity is already 0");
            } else {
                System.out.println("ℹ️ Info: Voucher has unlimited quantity (null)");
            }
        }

        // Xử lý tồn kho sản phẩm
        for (OrderItem item : order.getItems()) {
            ProductVariant variant = item.getProductVariant();
            Integer orderQuantity = item.getQuantity();

            System.out.println("📦 Product: " + variant.getProduct().getName() +
                              ", Size: " + variant.getSize().getName() +
                              ", Color: " + variant.getColor().getName() +
                              ", Current Stock: " + variant.getQuantity() +
                              ", Order Quantity: " + orderQuantity);

            try {
                String createdBy = "SYSTEM";
                stockMovementService.processSale(variant.getId(), orderQuantity, order.getOrderCode(), createdBy);

                System.out.println("✅ Stock updated and movement recorded for variant: " + variant.getId());
            } catch (Exception e) {
                System.err.println("❌ Error processing stock for variant " + variant.getId() + ": " + e.getMessage());
                throw new RuntimeException("Lỗi xử lý kho: " + e.getMessage());
            }
        }

        System.out.println("✅ Order items processing completed for order: " + order.getId());
    }

    @Override
    public Page<OrderDTO> searchOnlineOrders(String keyword, String paymentMethod,
                                           LocalDate dateStart, LocalDate dateEnd,
                                           int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        System.out.println("🌐 Searching ONLINE orders - This should NOT be called for completed page!");

        // Convert LocalDate to LocalDateTime for database query
        LocalDateTime dateTimeStart = dateStart != null ? dateStart.atStartOfDay() : null;
        LocalDateTime dateTimeEnd = dateEnd != null ? dateEnd.plusDays(1).atStartOfDay() : null;

        Page<Order> orders = orderRepository.searchOnlineOrders(keyword, paymentMethod, dateTimeStart, dateTimeEnd, pageable);
        return orders.map(this::mapToOrderDTO);
    }

    @Override
    public Page<OrderDTO> searchOfflineOrders(String keyword, String paymentMethod,
                                            LocalDate dateStart, LocalDate dateEnd,
                                            int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        System.out.println("🏪 Searching OFFLINE orders - This should NOT be called for completed page!");

        // Convert LocalDate to LocalDateTime for database query
        LocalDateTime dateTimeStart = dateStart != null ? dateStart.atStartOfDay() : null;
        LocalDateTime dateTimeEnd = dateEnd != null ? dateEnd.plusDays(1).atStartOfDay() : null;

        Page<Order> orders = orderRepository.searchOfflineOrders(keyword, paymentMethod, dateTimeStart, dateTimeEnd, pageable);
        return orders.map(this::mapToOrderDTO);
    }

    @Override
    public Page<OrderDTO> getCompletedOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        System.out.println("🔍 Getting completed orders - page: " + page + ", size: " + size);
        Page<Order> orders = orderRepository.findByStatusOrderByOrderDateDesc("COMPLETED", pageable);
        System.out.println("📊 Found " + orders.getTotalElements() + " completed orders");
        // Debug: in ra một số order đầu tiên
        orders.getContent().forEach(o ->
            System.out.println("   Order: " + o.getOrderCode() + " - Status: " + o.getStatus() + " - Type: " + o.getOrderType())
        );
        return orders.map(this::mapToOrderDTO);
    }

    @Override
    public Page<OrderDTO> searchCompletedOrders(String keyword, String paymentMethod,
                                              LocalDate dateStart, LocalDate dateEnd,
                                              int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        System.out.println("🔍 Searching completed orders with keyword: " + keyword + ", paymentMethod: " + paymentMethod);

        // Convert LocalDate to LocalDateTime for database query
        LocalDateTime dateTimeStart = dateStart != null ? dateStart.atStartOfDay() : null;
        LocalDateTime dateTimeEnd = dateEnd != null ? dateEnd.plusDays(1).atStartOfDay() : null;

        Page<Order> orders = orderRepository.searchCompletedOrders(keyword, paymentMethod, dateTimeStart, dateTimeEnd, pageable);
        System.out.println("📊 Found " + orders.getTotalElements() + " completed orders from search");
        // Debug: in ra một số order đầu tiên
        orders.getContent().forEach(o ->
            System.out.println("   Order: " + o.getOrderCode() + " - Status: " + o.getStatus() + " - Type: " + o.getOrderType())
        );
        return orders.map(this::mapToOrderDTO);
    }

    @Override
    public Page<OrderDTO> searchCompletedOrdersWithTypeFilter(String keyword, String paymentMethod, String orderTypeFilter,
                                                             LocalDate dateStart, LocalDate dateEnd,
                                                             int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        System.out.println("🔍 Searching completed orders with type filter: " + orderTypeFilter);

        // Convert LocalDate to LocalDateTime for database query
        LocalDateTime dateTimeStart = dateStart != null ? dateStart.atStartOfDay() : null;
        LocalDateTime dateTimeEnd = dateEnd != null ? dateEnd.plusDays(1).atStartOfDay() : null;

        // Convert orderTypeFilter to actual orderType for database
        String dbOrderType = null;
        if ("Online".equals(orderTypeFilter)) {
            dbOrderType = "ONLINE";
        } else if ("Offline".equals(orderTypeFilter)) {
            dbOrderType = "COUNTER";
        }
        // If orderTypeFilter is empty or null, dbOrderType remains null (search all)

        Page<Order> orders = orderRepository.searchCompletedOrdersWithTypeFilter(keyword, paymentMethod, dbOrderType, dateTimeStart, dateTimeEnd, pageable);
        System.out.println("📊 Found " + orders.getTotalElements() + " completed orders with type filter: " + orderTypeFilter);
        return orders.map(this::mapToOrderDTO);
    }

    @Override
    public Order findByOrderCode(String orderCode) {
        return orderRepository.findByOrderCode(orderCode).orElse(null);
    }

    @Override
    public Order findByOrderCodeWithItems(String orderCode) {
        return orderRepository.findByOrderCodeWithItems(orderCode).orElse(null);
    }
}



