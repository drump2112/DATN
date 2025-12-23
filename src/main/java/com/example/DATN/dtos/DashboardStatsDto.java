package com.example.DATN.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private Long totalProducts;
    private Map<String, Long> paymentMethodStats;
    private Double completionRate;
    private Double cancellationRate;
    private List<MonthlyRevenueDto> monthlyRevenue;
    private List<TopProductDto> topProducts;
    private Map<String, Long> orderStatusCounts;
    private List<QuarterlyProductSalesDto> quarterlyProductSales;
}