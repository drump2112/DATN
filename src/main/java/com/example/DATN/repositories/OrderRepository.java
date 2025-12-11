package com.example.DATN.repositories;

import java.math.BigDecimal;
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

  @EntityGraph(attributePaths = {"voucher", "items", "items.productVariant"})
  @Query("SELECT o FROM Order o WHERE o.id = :id")
  Optional<Order> findByIdWithVoucher(@Param("id") Integer id);

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

  // Dashboard statistics queries
  @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.status = 'COMPLETED'")
  BigDecimal findTotalRevenue();

  @Query(value = "SELECT COALESCE(SUM(ord.finalAmount), 0) FROM Orders ord WHERE ord.Status = 'COMPLETED' " +
         "AND MONTH(ord.OrderDate) = MONTH(GETDATE()) AND YEAR(ord.OrderDate) = YEAR(GETDATE())", nativeQuery = true)
  BigDecimal findCurrentMonthRevenue();

  @Query(value = "SELECT COALESCE(SUM(ord.finalAmount), 0) FROM Orders ord WHERE ord.Status = 'COMPLETED' " +
         "AND CAST(ord.OrderDate AS DATE) = :date", nativeQuery = true)
  BigDecimal findRevenueByDate(@Param("date") String date);

  // Query to get daily revenue within a date range - SQL Server compatible
  @Query(value = "SELECT FORMAT(ord.OrderDate, 'yyyy-MM-dd') as orderDate, " +
         "ISNULL(SUM(ord.finalAmount), 0) as revenue, " +
         "COUNT(*) as orderCount " +
         "FROM Orders ord " +
         "WHERE ord.Status = 'COMPLETED' " +
         "AND ord.OrderDate >= :startDate " +
         "AND ord.OrderDate <= DATEADD(DAY, 1, CAST(:endDate AS DATETIME)) " +
         "GROUP BY FORMAT(ord.OrderDate, 'yyyy-MM-dd') " +
         "ORDER BY FORMAT(ord.OrderDate, 'yyyy-MM-dd')", nativeQuery = true)
  List<Object[]> findDailyRevenueInRange(@Param("startDate") String startDate, @Param("endDate") String endDate);

  long countByStatus(String status);

  @Query(value = "SELECT MONTH(ord.OrderDate) as month, COALESCE(SUM(ord.finalAmount), 0) as revenue, COUNT(*) as orderCount " +
         "FROM Orders ord WHERE YEAR(ord.OrderDate) = :year AND ord.Status = 'COMPLETED' " +
         "GROUP BY MONTH(ord.OrderDate) ORDER BY MONTH(ord.OrderDate)", nativeQuery = true)
  List<Object[]> findMonthlyRevenue(@Param("year") int year);

  // Add method to get all monthly revenue without year filter to see what data exists
  @Query(value = "SELECT YEAR(ord.OrderDate) as year, MONTH(ord.OrderDate) as month, COALESCE(SUM(ord.finalAmount), 0) as revenue, COUNT(*) as orderCount " +
         "FROM Orders ord WHERE ord.Status = 'COMPLETED' " +
         "GROUP BY YEAR(ord.OrderDate), MONTH(ord.OrderDate) ORDER BY YEAR(ord.OrderDate), MONTH(ord.OrderDate)", nativeQuery = true)
  List<Object[]> findAllMonthlyRevenue();

  // Method to check what years have completed orders
  @Query(value = "SELECT DISTINCT YEAR(ord.OrderDate) as year FROM Orders ord WHERE ord.Status = 'COMPLETED' ORDER BY year DESC", nativeQuery = true)
  List<Integer> findAvailableYears();

  @Query(value = "SELECT TOP 5 p.id, p.Name, p.ProductCode, SUM(oi.quantity) as totalSold, " +
         "c.Name as colorName, c.ColorCode, pvi.ImageUrl, p.thumbnail " +
         "FROM OrderItems oi " +
         "INNER JOIN ProductVariants pv ON oi.ProductVariantId = pv.id " +
         "INNER JOIN Products p ON pv.ProductID = p.id " +
         "INNER JOIN Colors c ON pv.ColorID = c.id " +
         "LEFT JOIN ProductVariantImage pvi ON p.id = pvi.ProductId AND c.id = pvi.ColorId AND pvi.SortOrder = 1 " +
         "INNER JOIN Orders ord ON oi.OrderId = ord.id " +
         "WHERE ord.Status = 'COMPLETED' " +
         "AND MONTH(ord.OrderDate) = MONTH(GETDATE()) " +
         "AND YEAR(ord.OrderDate) = YEAR(GETDATE()) " +
         "GROUP BY p.id, p.Name, p.ProductCode, c.Name, c.ColorCode, pvi.ImageUrl, p.thumbnail " +
         "ORDER BY SUM(oi.quantity) DESC", nativeQuery = true)
  List<Object[]> findTopProducts();

  @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
  List<Object[]> findOrderStatusCounts();

  // Payment method statistics - only count COMPLETED orders
  @Query("SELECT o.paymentMethod, COUNT(o) FROM Order o WHERE o.paymentMethod IS NOT NULL AND o.status = 'COMPLETED' GROUP BY o.paymentMethod")
  List<Object[]> findPaymentMethodCounts();

  @Query(value = "WITH QuarterlySales AS ( " +
         "  SELECT " +
         "    CASE " +
         "      WHEN MONTH(ord.OrderDate) IN (1,2,3) THEN 1 " +
         "      WHEN MONTH(ord.OrderDate) IN (4,5,6) THEN 2 " +
         "      WHEN MONTH(ord.OrderDate) IN (7,8,9) THEN 3 " +
         "      ELSE 4 " +
         "    END as quarter, " +
         "    p.Name as productName, " +
         "    SUM(oi.quantity) as totalSold, " +
         "    ROW_NUMBER() OVER ( " +
         "      PARTITION BY CASE " +
         "        WHEN MONTH(ord.OrderDate) IN (1,2,3) THEN 1 " +
         "        WHEN MONTH(ord.OrderDate) IN (4,5,6) THEN 2 " +
         "        WHEN MONTH(ord.OrderDate) IN (7,8,9) THEN 3 " +
         "        ELSE 4 " +
         "      END " +
         "      ORDER BY SUM(oi.quantity) DESC " +
         "    ) as rn " +
         "  FROM OrderItems oi " +
         "  INNER JOIN ProductVariants pv ON oi.ProductVariantId = pv.id " +
         "  INNER JOIN Products p ON pv.ProductID = p.id " +
         "  INNER JOIN Orders ord ON oi.OrderId = ord.id " +
         "  WHERE YEAR(ord.OrderDate) = :year AND ord.Status = 'COMPLETED' " +
         "  GROUP BY " +
         "    CASE " +
         "      WHEN MONTH(ord.OrderDate) IN (1,2,3) THEN 1 " +
         "      WHEN MONTH(ord.OrderDate) IN (4,5,6) THEN 2 " +
         "      WHEN MONTH(ord.OrderDate) IN (7,8,9) THEN 3 " +
         "      ELSE 4 " +
         "    END, p.id, p.Name " +
         ") " +
         "SELECT quarter, productName, totalSold FROM QuarterlySales WHERE rn = 1 ORDER BY quarter", nativeQuery = true)
  List<Object[]> findQuarterlyProductSales(@Param("year") int year);
}
