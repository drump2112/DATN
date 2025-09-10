package com.example.DATN.models;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "ProductVariants")
@Builder
public class ProductVariant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "VariantCode", nullable = false, unique = true, length = 20)
	private String variantCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ProductID")
	private Product product;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SizeID")
	private Size size;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ColorID")
	private Color color;

	@Column(name = "SKU", unique = true, length = 100)
	private String sku;

	@Column(name = "Price", nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@Column(name = "Quantity", nullable = false)
	private Integer quantity;

	@Column(name = "Status")
	private Boolean status;

	@OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<StockMovement> stockMovements;

}
