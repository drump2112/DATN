package com.example.DATN.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Discounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Discount {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@Column(unique = true, nullable = false)
	private String code;

	// Loại giảm giá: PERCENT, AMOUNT, FREESHIP, BOGO...
	private String discountType;

	// Giá trị chính (ví dụ 10% hoặc 50000 VND)
	private BigDecimal discountValue;

	// Hạn mức áp dụng
	private BigDecimal minOrderAmount;
	private BigDecimal maxDiscountValue;

	private LocalDateTime startDate;
	private LocalDateTime endDate;

	private Boolean isActive = true;

	// Giới hạn sử dụng
	private Integer usageLimit; // Tổng lượt dùng toàn hệ thống
	private Integer perUserLimit; // Số lần mỗi user dùng

	// Cấu hình đặc biệt dưới dạng JSON (BOGO, Freeship,...)
	@Column(columnDefinition = "NVARCHAR(MAX)")
	private String extraConfig;

	private LocalDateTime createdAt = LocalDateTime.now();
	private LocalDateTime updatedAt;
}
