package com.example.DATN.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodStatsDto {
    private Long cashCount;
    private Long bankTransferCount;
    private Long vnpayCount;
    private Long totalOrders;
    private Double cashPercentage;
    private Double bankTransferPercentage;
    private Double vnpayPercentage;
}