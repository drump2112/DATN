package com.example.DATN.models;

import java.util.Date;
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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Products")
@Builder(toBuilder = true)
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "ProductCode", nullable = false, unique = true, length = 10)
	private String productCode;

	@Column(name = "Name", nullable = false, length = 100)
	private String name;

	@Column(name = "Description", columnDefinition = "nvarchar(max)")
	private String description;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BrandId")
	private Brand brand;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CategoryId")
	private Category category;

	@Column(name = "CreateAt")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createAt;

	@Column(name = "IsActive")
	private Boolean isActive;

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
	private List<ProductVariant> variants;

	@Column(name = "Thumbnail")
	private String thumbnail;
}
