package com.example.DATN.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.DATN.models.Order;

public interface OrderRepository extends JpaRepository<Order, Integer> {

  Page<Order> findByOrderType(String orderType, Pageable pageable);

  long countByOrderDateBetween(LocalDateTime start, LocalDateTime end);

  @EntityGraph(attributePaths = {"items", "items.productVariant", "items.productVariant.product",
                                   "items.productVariant.color", "items.productVariant.size", "user", "voucher"})
  @Query("SELECT o FROM Order o WHERE o.orderCode = :orderCode")
  Optional<Order> findByOrderCodeWithItems(@Param("orderCode") String orderCode);

  Optional<Order> findByOrderCode(String orderCode);

  Page<Order> findByUserIdOrderByOrderDateDesc(Integer userId, Pageable pageable);

  @Query("SELECT o FROM Order o LEFT JOIN o.user u WHERE " +
         "(:keyword IS NULL OR :keyword = '' OR " +
         "LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
         "LOWER(o.customerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
         "LOWER(o.shippingPhone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
         "(u.userCode IS NOT NULL AND LOWER(u.userCode) LIKE LOWER(CONCAT('%', :keyword, '%')))) AND " +
         "(:orderType IS NULL OR :orderType = '' OR o.orderType = :orderType) AND " +
         "(:paymentMethod IS NULL OR :paymentMethod = '' OR o.paymentMethod = :paymentMethod) AND " +
         "(:dateStart IS NULL OR o.orderDate >= :dateStart) AND " +
         "(:dateEnd IS NULL OR o.orderDate < :dateEnd)")
  Page<Order> searchOrders(@Param("keyword") String keyword,
                          @Param("orderType") String orderType,
                          @Param("paymentMethod") String paymentMethod,
                          @Param("dateStart") LocalDateTime dateStart,
                          @Param("dateEnd") LocalDateTime dateEnd,
                          Pageable pageable);

  @Query("SELECT o FROM Order o LEFT JOIN o.user u WHERE " +
         "o.orderType = 'ONLINE' AND " +
         "(:keyword IS NULL OR :keyword = '' OR " +
         "LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
         "LOWER(o.customerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
         "LOWER(o.shippingPhone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
         "(u.userCode IS NOT NULL AND LOWER(u.userCode) LIKE LOWER(CONCAT('%', :keyword, '%')))) AND " +
         "(:paymentMethod IS NULL OR :paymentMethod = '' OR o.paymentMethod = :paymentMethod) AND " +
         "(:dateStart IS NULL OR o.orderDate >= :dateStart) AND " +
         "(:dateEnd IS NULL OR o.orderDate < :dateEnd)")
  Page<Order> searchOnlineOrders(@Param("keyword") String keyword,
                                @Param("paymentMethod") String paymentMethod,
                                @Param("dateStart") LocalDateTime dateStart,
                                @Param("dateEnd") LocalDateTime dateEnd,
                                Pageable pageable);

  @Query("SELECT o FROM Order o LEFT JOIN o.user u WHERE " +
         "o.orderType = 'COUNTER' AND " +
         "(:keyword IS NULL OR :keyword = '' OR " +
         "LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
         "LOWER(o.customerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
         "LOWER(o.shippingPhone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
         "(u.userCode IS NOT NULL AND LOWER(u.userCode) LIKE LOWER(CONCAT('%', :keyword, '%')))) AND " +
         "(:paymentMethod IS NULL OR :paymentMethod = '' OR o.paymentMethod = :paymentMethod) AND " +
         "(:dateStart IS NULL OR o.orderDate >= :dateStart) AND " +
         "(:dateEnd IS NULL OR o.orderDate < :dateEnd)")
  Page<Order> searchOfflineOrders(@Param("keyword") String keyword,
                                 @Param("paymentMethod") String paymentMethod,
                                 @Param("dateStart") LocalDateTime dateStart,
                                 @Param("dateEnd") LocalDateTime dateEnd,
                                 Pageable pageable);

  Page<Order> findByStatusOrderByOrderDateDesc(String status, Pageable pageable);

  @Query("SELECT o FROM Order o LEFT JOIN o.user u WHERE " +
         "o.status = 'COMPLETED' AND " +
         "(:keyword IS NULL OR :keyword = '' OR " +
         "LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
         "LOWER(o.customerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
         "LOWER(o.shippingPhone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
         "(u.userCode IS NOT NULL AND LOWER(u.userCode) LIKE LOWER(CONCAT('%', :keyword, '%')))) AND " +
         "(:paymentMethod IS NULL OR :paymentMethod = '' OR o.paymentMethod = :paymentMethod) AND " +
         "(:dateStart IS NULL OR o.orderDate >= :dateStart) AND " +
         "(:dateEnd IS NULL OR o.orderDate < :dateEnd)")
  Page<Order> searchCompletedOrders(@Param("keyword") String keyword,
                                   @Param("paymentMethod") String paymentMethod,
                                   @Param("dateStart") LocalDateTime dateStart,
                                   @Param("dateEnd") LocalDateTime dateEnd,
                                   Pageable pageable);

  @Query("SELECT o FROM Order o LEFT JOIN o.user u WHERE " +
         "o.status = 'COMPLETED' AND " +
         "(:orderType IS NULL OR :orderType = '' OR o.orderType = :orderType) AND " +
         "(:keyword IS NULL OR :keyword = '' OR " +
         "LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
         "LOWER(o.customerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
         "LOWER(o.shippingPhone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
         "(u.userCode IS NOT NULL AND LOWER(u.userCode) LIKE LOWER(CONCAT('%', :keyword, '%')))) AND " +
         "(:paymentMethod IS NULL OR :paymentMethod = '' OR o.paymentMethod = :paymentMethod) AND " +
         "(:dateStart IS NULL OR o.orderDate >= :dateStart) AND " +
         "(:dateEnd IS NULL OR o.orderDate < :dateEnd)")
  Page<Order> searchCompletedOrdersWithTypeFilter(@Param("keyword") String keyword,
                                                  @Param("paymentMethod") String paymentMethod,
                                                  @Param("orderType") String orderType,
                                                  @Param("dateStart") LocalDateTime dateStart,
                                                  @Param("dateEnd") LocalDateTime dateEnd,
                                                  Pageable pageable);
}
