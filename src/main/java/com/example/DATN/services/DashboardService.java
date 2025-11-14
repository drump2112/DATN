package com.example.DATN.services;

import com.example.DATN.dtos.*;
import com.example.DATN.repositories.OrderRepository;
import com.example.DATN.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public DashboardStatsDto getDashboardStats() {
        logger.info("Starting to collect dashboard statistics");
        int currentYear = Year.now().getValue();

        try {
            // Check what years have data
            List<Integer> availableYears = orderRepository.findAvailableYears();
            logger.info("Available years with completed orders: {}", availableYears);

            // Get monthly revenue for the most recent year with data
            List<MonthlyRevenueDto> monthlyRevenue = new ArrayList<>();
            if (!availableYears.isEmpty()) {
                int targetYear = availableYears.get(0); // Most recent year
                logger.info("Using year {} for monthly revenue", targetYear);
                monthlyRevenue = getMonthlyRevenue(targetYear);
            }

            // If still empty, get all monthly revenue regardless of year
            if (monthlyRevenue.isEmpty()) {
                logger.info("No data for specific year, getting all monthly revenue");
                monthlyRevenue = getAllMonthlyRevenue();
            }

            DashboardStatsDto stats = DashboardStatsDto.builder()
                    .totalOrders(getTotalOrders())
                    .totalRevenue(getTotalRevenue())
                    .totalProducts(getTotalProducts())
                    .paymentMethodStats(getPaymentMethodStats())
                    .completionRate(getCompletionRate())
                    .cancellationRate(getCancellationRate())
                    .monthlyRevenue(monthlyRevenue)
                    .topProducts(getTopProducts())
                    .orderStatusCounts(getOrderStatusCounts())
                    .quarterlyProductSales(getQuarterlyProductSales(currentYear))
                    .build();

            logger.info("Dashboard stats collected successfully: totalOrders={}, totalRevenue={}, monthlyRevenue.size={}, paymentMethodStats={}",
                stats.getTotalOrders(), stats.getTotalRevenue(), stats.getMonthlyRevenue().size(), stats.getPaymentMethodStats());

            return stats;
        } catch (Exception e) {
            logger.error("Error collecting dashboard statistics", e);
            throw e;
        }
    }

    private Long getTotalOrders() {
        return orderRepository.count();
    }

    private BigDecimal getTotalRevenue() {
        try {
            BigDecimal result = orderRepository.findCurrentMonthRevenue();
            logger.info("Current month revenue: {}", result);
            return result != null ? result : BigDecimal.ZERO;
        } catch (Exception e) {
            logger.error("Error getting current month revenue", e);
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getRevenueByDate(String date) {
        try {
            BigDecimal result = orderRepository.findRevenueByDate(date);
            logger.info("Revenue for date {}: {}", date, result);
            return result != null ? result : BigDecimal.ZERO;
        } catch (Exception e) {
            logger.error("Error getting revenue for date {}", date, e);
            return BigDecimal.ZERO;
        }
    }

    private Long getTotalProducts() {
        return productRepository.count();
    }

    private Map<String, Long> getPaymentMethodStats() {
        try {
            List<Object[]> results = orderRepository.findPaymentMethodCounts();
            Map<String, Long> paymentStats = new HashMap<>();

            logger.info("Found {} payment method records", results.size());

            for (Object[] result : results) {
                String paymentMethod = (String) result[0];
                Long count = ((Number) result[1]).longValue();

                logger.info("Payment method debug: '{}' count={}", paymentMethod, count);

                // Normalize payment method names
                String normalizedMethod = normalizePaymentMethod(paymentMethod);

                // Sum up counts for same normalized method
                paymentStats.put(normalizedMethod, paymentStats.getOrDefault(normalizedMethod, 0L) + count);

                logger.info("Payment method normalized: '{}' total={}",
                    normalizedMethod, paymentStats.get(normalizedMethod));
            }

            logger.info("Final payment method stats: {}", paymentStats);
            return paymentStats;
        } catch (Exception e) {
            logger.error("Error getting payment method stats", e);
            // Return default empty stats to prevent dashboard crash
            Map<String, Long> defaultStats = new HashMap<>();
            defaultStats.put("Tiền mặt", 0L);
            defaultStats.put("Chuyển khoản", 0L);
            defaultStats.put("VNPay", 0L);
            return defaultStats;
        }
    }

    private String normalizePaymentMethod(String paymentMethod) {
        if (paymentMethod == null) return "Khác";

        switch (paymentMethod.toUpperCase()) {
            case "CASH":
            case "COUNTER":
                return "Tiền mặt";
            case "TRANSFER":
            case "BANK_TRANSFER":
                return "Chuyển khoản";
            case "VNPAY":
            case "VN_PAY":
                return "VNPay";
            default:
                return paymentMethod;
        }
    }

    private Double getCompletionRate() {
        long totalOrders = orderRepository.count();
        if (totalOrders == 0) return 0.0;

        long completedOrders = orderRepository.countByStatus("COMPLETED");
        return (double) completedOrders / totalOrders * 100;
    }

    private Double getCancellationRate() {
        long totalOrders = orderRepository.count();
        if (totalOrders == 0) return 0.0;

        long cancelledOrders = orderRepository.countByStatus("CANCELLED");
        return (double) cancelledOrders / totalOrders * 100;
    }

    private List<MonthlyRevenueDto> getMonthlyRevenue(int year) {
        try {
            List<Object[]> results = orderRepository.findMonthlyRevenue(year);
            List<MonthlyRevenueDto> monthlyRevenue = new ArrayList<>();

            logger.info("Found {} monthly revenue records for year {}", results.size(), year);

            // Create a map for quick lookup of existing data
            Map<Integer, MonthlyRevenueDto> dataMap = new HashMap<>();

            // Process existing data from database
            for (Object[] result : results) {
                int month = ((Number) result[0]).intValue();
                BigDecimal revenue = (BigDecimal) result[1];
                Long orderCount = ((Number) result[2]).longValue();

                dataMap.put(month, MonthlyRevenueDto.builder()
                        .month(month)
                        .year(year)
                        .revenue(revenue != null ? revenue : BigDecimal.ZERO)
                        .orderCount(orderCount != null ? orderCount : 0L)
                        .build());
            }

            // Ensure all 12 months are included (1-12)
            for (int month = 1; month <= 12; month++) {
                if (dataMap.containsKey(month)) {
                    // Use existing data
                    monthlyRevenue.add(dataMap.get(month));
                } else {
                    // Create empty data for months without orders
                    monthlyRevenue.add(MonthlyRevenueDto.builder()
                            .month(month)
                            .year(year)
                            .revenue(BigDecimal.ZERO)
                            .orderCount(0L)
                            .build());
                }
            }

            logger.info("Processed {} monthly revenue entries (including all 12 months)", monthlyRevenue.size());
            return monthlyRevenue;
        } catch (Exception e) {
            logger.error("Error getting monthly revenue for year {}", year, e);
            // Return 12 months with zero data if there's an error
            List<MonthlyRevenueDto> emptyMonths = new ArrayList<>();
            for (int month = 1; month <= 12; month++) {
                emptyMonths.add(MonthlyRevenueDto.builder()
                        .month(month)
                        .year(year)
                        .revenue(BigDecimal.ZERO)
                        .orderCount(0L)
                        .build());
            }
            return emptyMonths;
        }
    }

    private List<MonthlyRevenueDto> getAllMonthlyRevenue() {
        try {
            List<Object[]> results = orderRepository.findAllMonthlyRevenue();
            List<MonthlyRevenueDto> monthlyRevenue = new ArrayList<>();

            logger.info("Found {} total monthly revenue records", results.size());

            for (Object[] result : results) {
                int year = ((Number) result[0]).intValue();
                int month = ((Number) result[1]).intValue();
                BigDecimal revenue = (BigDecimal) result[2];
                Long orderCount = ((Number) result[3]).longValue();

                monthlyRevenue.add(MonthlyRevenueDto.builder()
                        .month(month)
                        .year(year)
                        .revenue(revenue != null ? revenue : BigDecimal.ZERO)
                        .orderCount(orderCount != null ? orderCount : 0L)
                        .build());
            }

            logger.info("Processed {} total monthly revenue entries", monthlyRevenue.size());
            return monthlyRevenue;
        } catch (Exception e) {
            logger.error("Error getting all monthly revenue", e);
            return new ArrayList<>();
        }
    }

    private List<TopProductDto> getTopProducts() {
        try {
            List<Object[]> results = orderRepository.findTopProducts();
            return results.stream()
                    .map(result -> TopProductDto.builder()
                            .productId(((Number) result[0]).intValue())
                            .productName((String) result[1])
                            .productCode((String) result[2])
                            .totalSold(((Number) result[3]).longValue())
                            .colorName((String) result[4])
                            .colorCode((String) result[5])
                            .variantImageUrl((String) result[6])
                            .thumbnail((String) result[7])
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting top products", e);
            // Trả về list rỗng nếu có lỗi, không fake data
            return new ArrayList<>();
        }
    }

    private Map<String, Long> getOrderStatusCounts() {
        try {
            List<Object[]> results = orderRepository.findOrderStatusCounts();
            Map<String, Long> statusCounts = new HashMap<>();

            for (Object[] result : results) {
                String status = (String) result[0];
                Long count = ((Number) result[1]).longValue();
                statusCounts.put(status, count);
            }

            return statusCounts;
        } catch (Exception e) {
            // Trả về map rỗng nếu có lỗi, không fake data
            return new HashMap<>();
        }
    }

    private List<QuarterlyProductSalesDto> getQuarterlyProductSales(int year) {
        try {
            List<Object[]> results = orderRepository.findQuarterlyProductSales(year);
            return results.stream()
                    .map(result -> QuarterlyProductSalesDto.builder()
                            .quarter(((Number) result[0]).intValue())
                            .year(year)
                            .productName((String) result[1])
                            .totalSold(((Number) result[2]).longValue())
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Return empty list if there's an error
            return new ArrayList<>();
        }
    }
}