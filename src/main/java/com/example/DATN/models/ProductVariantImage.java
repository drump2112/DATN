package com.example.DATN.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ProductVariantImage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantImage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ProductVariantId")
	private ProductVariant productVariant;

	@Column(name = "ImageUrl", nullable = false, length = 255)
	private String imageUrl;

	@Column(name = "SortOrder")
	private Integer sortOrder;
}
