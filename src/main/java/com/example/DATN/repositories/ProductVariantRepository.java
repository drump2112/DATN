package com.example.DATN.repositories;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.example.DATN.models.ProductVariant;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductVariantRepository
		extends JpaRepository<ProductVariant, Integer>, JpaSpecificationExecutor<ProductVariant> {

	@EntityGraph(attributePaths = {
			"product",
			"product.brand",
			"product.category",
			"size",
			"color"
	})
	Page<ProductVariant> findAll(Pageable pageable);

	List<ProductVariant> findByProductId(Integer productId);

	boolean existsByVariantCode(String variantCode);

	boolean existsByProductIdAndColorId(Integer productId, Integer colorId);

	boolean existsByProductIdAndColorIdAndSizeId(Integer productId, Integer colorId, Integer sizeId);

	@Query("SELECT MIN(pv.price) FROM ProductVariant pv WHERE pv.product.id = :productId AND pv.color.id = :colorId")
	Optional<BigDecimal> findPriceByProductIdAndColorId(@Param("productId") Integer productId,
			@Param("colorId") Integer colorId);

	@Query("SELECT MAX(v.variantCode) FROM ProductVariant v")
	String findMaxVariantCode();

	@Query("SELECT pv FROM ProductVariant pv " +
			"JOIN FETCH pv.product p " +
			"JOIN FETCH p.brand " +
			"JOIN FETCH p.category " +
			"JOIN FETCH pv.color " +
			"JOIN FETCH pv.size " +
			"WHERE pv.id = :id")
	Optional<ProductVariant> findDetailById(@Param("id") Integer id);

	@Query("""
			    SELECT pv FROM ProductVariant pv
			    JOIN FETCH pv.product p
			    JOIN FETCH pv.color c
			    JOIN FETCH pv.size s
			    WHERE pv.status = true
			      AND pv.quantity >= 0
			      AND (
			        LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
			        OR LOWER(pv.variantCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
			      )
			""")
	List<ProductVariant> searchByKeyword(@Param("keyword") String keyword);

	List<ProductVariant> findByStatusTrue();

	Page<ProductVariant> findProductVariantsByProduct_Id(Integer productId, Pageable pageable);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT v FROM ProductVariant v WHERE v.id = :id")
	ProductVariant findByIdForUpdate(@Param("id") Integer id);

	// Lấy các sản phẩm bán chạy nhất dựa trên tổng số lượng đã bán trong OrderItem (chỉ tính đơn hoàn thành)
	@Query("""
			SELECT pv.product.id, SUM(oi.quantity) as totalSold
			FROM OrderItem oi
			JOIN oi.productVariant pv
			JOIN oi.order o
			WHERE o.status = 'COMPLETED'
			  AND pv.product.isActive = true
			GROUP BY pv.product.id
			ORDER BY totalSold DESC
			""")
	List<Object[]> findBestSellingProducts(Pageable pageable);

	// Lấy các biến thể sản phẩm bán chạy nhất (ProductVariant) = Lấy theo tháng gần nhát hoặc quý
	@Query("""
			SELECT pv.id, SUM(oi.quantity) as totalSold
			FROM OrderItem oi
			JOIN oi.productVariant pv
			JOIN oi.order o
			WHERE o.status = 'COMPLETED'
			  AND pv.status = true
			  AND pv.product.isActive = true
			GROUP BY pv.id
			ORDER BY totalSold DESC
			""")
	List<Object[]> findBestSellingVariants(Pageable pageable);

	// Lấy các biến thể sản phẩm mới nhất (ProductVariant)
	@Query("""
			SELECT pv
			FROM ProductVariant pv
			JOIN FETCH pv.product p
			JOIN FETCH pv.color c
			JOIN FETCH pv.size s
			JOIN FETCH p.brand b
			JOIN FETCH p.category cat
			WHERE pv.status = true
			  AND p.isActive = true
			ORDER BY p.createAt DESC
			""")
	List<ProductVariant> findNewestVariants(Pageable pageable);
}
