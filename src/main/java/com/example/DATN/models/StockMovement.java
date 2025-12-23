package com.example.DATN.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "StockMovements")
public class StockMovement {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	// Liên kết nhiều - một: nhiều StockMovement thuộc về 1 ProductVariant
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ProductVariantId", nullable = false)
	private ProductVariant productVariant;

	@Column(name = "Quantity", nullable = false)
	private Integer quantity;

	@Column(name = "MovementType", length = 20, nullable = false)
	private String movementType; // IN, OUT, RETURN, DAMAGE

	@Column(name = "Note")
	private String note;

	@Column(name = "CreatedAt", columnDefinition = "DATETIME DEFAULT GETDATE()")
	private LocalDateTime createdAt;

	@Column(name = "CreatedBy", length = 100)
	private String createdBy;
}
