package com.example.DATN.services.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.example.DATN.dtos.ProductDTO;
import com.example.DATN.exception.BusinessException;
import com.example.DATN.models.Brand;
import com.example.DATN.models.Category;
import com.example.DATN.models.Product;
import com.example.DATN.repositories.BrandRepository;
import com.example.DATN.repositories.CategoryRepository;
import com.example.DATN.repositories.ProductRepository;
import com.example.DATN.repositories.ProductVariantRepository;
import com.example.DATN.request.ProductRequest;
import com.example.DATN.services.ImageService;
import com.example.DATN.services.ProductService;
import com.example.DATN.specifications.ProductSpecification;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ProductVariantRepository productVariantRepository;

	@Autowired
	private BrandRepository brandRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private ImageService imageService;

	@Override
	public Page<ProductDTO> getAllProducts(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);

		Page<Product> product = productRepository.findAll(pageable);

		return product.map(entity -> {
			ProductDTO dto = modelMapper.map(entity, ProductDTO.class);
			if (entity.getBrand() != null && entity.getCategory() != null) {
				dto.setBrandId(entity.getBrand().getId());
				dto.setBrandName(entity.getBrand().getName());
				dto.setCategoryId(entity.getCategory().getId());
				dto.setCategoryName(entity.getCategory().getName());
			}
			// Tính tổng số lượng tồn kho của tất cả biến thể từ database
			Integer totalQty = productVariantRepository.getTotalQuantityByProductId(entity.getId());
			dto.setTotalQuantity(totalQty != null ? totalQty : 0);
			return dto;
		});
	}

	@Override
	public boolean toggleStatus(Integer id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new BusinessException("Không tìm thấy sản phẩm"));
		product.setIsActive(!product.getIsActive());
		productRepository.save(product);
		return product.getIsActive();
	}

	@Override
	public ProductDTO getProductDTOById(Integer id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new BusinessException("Không tìm thấy sản phẩm"));

		ProductDTO dto = modelMapper.map(product, ProductDTO.class);

		if (product.getBrand() != null) {
			dto.setBrandId(product.getBrand().getId());
			dto.setBrandName(product.getBrand().getName());
		}

		if (product.getCategory() != null) {
			dto.setCategoryId(product.getCategory().getId());
			dto.setCategoryName(product.getCategory().getName());
		}

		return dto;
	}

	@Override
	public Page<ProductDTO> searchProducts(String keyword, Boolean isActive, Pageable pageable) {
		Specification<Product> spec = Specification
				.where(ProductSpecification.containsKeyword(keyword))
				.and(ProductSpecification.isActive(isActive));

		Page<Product> products = productRepository.findAll(spec, pageable);

		return products.map(entity -> {
			ProductDTO dto = modelMapper.map(entity, ProductDTO.class);

			if (entity.getBrand() != null && entity.getCategory() != null) {
				dto.setBrandName(entity.getBrand().getName());
				dto.setCategoryName(entity.getCategory().getName());
			}
			// Tính tổng số lượng tồn kho của tất cả biến thể từ database
			Integer totalQty = productVariantRepository.getTotalQuantityByProductId(entity.getId());
			dto.setTotalQuantity(totalQty != null ? totalQty : 0);
			return dto;
		});

	}

	@Override
	public boolean addProduct(ProductRequest productRequest) {
		// Trim dữ liệu input
		productRequest.setName(productRequest.getName() != null ? productRequest.getName().trim() : null);
		productRequest.setDescription(productRequest.getDescription() != null ? productRequest.getDescription().trim() : null);

		Product product = fromProductRequest(productRequest);

		if (productRepository.existsByName(productRequest.getName())) {
			throw new BusinessException("Tên sản phẩm đã tồn tại");
		}

		productRepository.save(product);
		return true;

	}

	@Override
	public boolean updateProduct(Integer id, ProductRequest productRequest) {
		try {
			// Trim dữ liệu input
			productRequest.setName(productRequest.getName() != null ? productRequest.getName().trim() : null);
			productRequest.setDescription(productRequest.getDescription() != null ? productRequest.getDescription().trim() : null);

			// Tìm sản phẩm theo ID
			Product existingProduct = productRepository.findById(id)
					.orElseThrow(() -> new BusinessException("Không tìm thấy sản phẩm"));

			// Kiểm tra trùng tên (loại trừ chính sản phẩm hiện tại)
			if (!existingProduct.getName().equals(productRequest.getName())) {
				List<Product> duplicateProducts = productRepository.findByNameContainingIgnoreCase(productRequest.getName());
				boolean isDuplicate = duplicateProducts.stream()
					.anyMatch(p -> p.getName().equals(productRequest.getName()) && !p.getId().equals(id));
				if (isDuplicate) {
					throw new BusinessException("Tên sản phẩm đã tồn tại");
				}
			}

			// Lấy brand mới
			Brand brand = brandRepository.findById(productRequest.getBrandId())
					.orElseThrow(() -> new BusinessException("Không tìm thấy thương hiệu"));

			// Lấy category mới
			Category category = categoryRepository.findById(productRequest.getCategoryId())
					.orElseThrow(() -> new BusinessException("Không tìm thấy danh mục"));

			// Xử lý thumbnail
			String thumbnailPath = handleUploadThumbnail(productRequest.getThumbnail(), existingProduct.getThumbnail());

			// Tạo sản phẩm mới với builder, giữ nguyên các trường không cần cập nhật
			Product updatedProduct = existingProduct.toBuilder()
					.name(productRequest.getName())
					.description(productRequest.getDescription())
					.brand(brand)
					.category(category)
					.thumbnail(thumbnailPath)
					.build();

			// Lưu sản phẩm
			productRepository.save(updatedProduct);
			return true;
		} catch (BusinessException e) {
			throw e; // Ném lại BusinessException để giữ nguyên message
		} catch (Exception e) {
			throw new BusinessException("Cập nhật sản phẩm thất bại");
		}
	}

	private Product fromProductRequest(ProductRequest req) {

		Product.ProductBuilder productBuilder = Product.builder()
				.name(req.getName())
				.description(req.getDescription())
				.createAt(new Date())
				.isActive(true); // default

		Brand brand = brandRepository.findById(req.getBrandId())
				.orElseThrow(() -> new BusinessException("Không tìm thấy thương hiệu"));
		productBuilder.brand(brand);

		Category category = categoryRepository.findById(req.getCategoryId())
				.orElseThrow(() -> new BusinessException("Không tìm thấy danh mục"));
		productBuilder.category(category);

		String productCode = generateProductCode();
		productBuilder.productCode(productCode);

		try {
			if (req.getThumbnail() != null && !req.getThumbnail().isEmpty()) {
				String thumbnailPath = uploadThumbnail(req.getThumbnail());
				productBuilder.thumbnail(thumbnailPath);
			}
		} catch (Exception e) {
			throw new BusinessException("Lỗi khi xử lý ảnh sản phẩm");
		}

		return productBuilder.build();
	}

	private String generateProductCode() {
		int nextNumber = 1;

		// Bắt đầu từ số 1, kiểm tra đến khi tìm được mã chưa tồn tại
		while (true) {
			String code = String.format("SP-%03d", nextNumber);
			if (!productRepository.existsByProductCode(code)) {
				return code;
			}
			nextNumber++;
		}
	}

	@Override
	public List<ProductDTO> getProducts(String keyword) {
		List<Product> products;

		if (keyword != null && !keyword.isBlank()) {
			products = productRepository.findByNameContainingIgnoreCase(keyword);
		} else {
			products = productRepository.findAll();
		}

		return products.stream()
				.map(Product -> ProductDTO.builder()
						.id(Product.getId())
						.productCode(Product.getProductCode())
						.name(Product.getName())
						.build())
				.collect(Collectors.toList());

	}

	// @Override
	// public List<ProductDTO> getProductActive() {
	// return productRepository.findByIsActive(true).stream()
	// .map(entity -> {
	// ProductDTO dto = modelMapper.map(entity, ProductDTO.class);

	// if (entity.getVariants() != null && !entity.getVariants().isEmpty()) {
	// double minPrice = entity.getVariants().stream()
	// .mapToDouble(v -> v.getPrice().doubleValue())
	// .min()
	// .orElse(0);
	// double maxPrice = entity.getVariants().stream()
	// .mapToDouble(v -> v.getPrice().doubleValue())
	// .max()
	// .orElse(0);
	// dto.setMinPrice(minPrice);
	// dto.setMaxPrice(maxPrice);
	// }

	// return dto;
	// })
	// .toList();
	// }

	@Override
	public List<ProductDTO> getProductActive() {
		return productRepository.findByIsActive(true).stream()
				.filter(entity -> entity.getVariants() != null && !entity.getVariants().isEmpty()) // Lọc sản phẩm có biến thể
				.map(entity -> {
					ProductDTO dto = modelMapper.map(entity, ProductDTO.class);

					double minPrice = entity.getVariants().stream()
							.mapToDouble(v -> v.getPrice().doubleValue())
							.min()
							.orElse(0);
					double maxPrice = entity.getVariants().stream()
							.mapToDouble(v -> v.getPrice().doubleValue())
							.max()
							.orElse(0);
					dto.setMinPrice(minPrice);
					dto.setMaxPrice(maxPrice);

					return dto;
				})
				.toList();
	}

	@Override
	public ProductDTO getById(Integer id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new BusinessException("Không tìm thấy sản phẩm"));
		return modelMapper.map(product, ProductDTO.class);
	}

	@Override
	public long countAll() {
		return productRepository.count();
	}

	private String uploadThumbnail(MultipartFile thumbnail) {
		try {
			return imageService.saveImage(thumbnail, "product");
		} catch (IOException e) {
			throw new BusinessException("Lỗi khi lưu ảnh sản phẩm");
		}
	}

	private String handleUploadThumbnail(MultipartFile thumbnail, String currentThumbnailPath) {
		if (thumbnail != null && !thumbnail.isEmpty()) {
			try {
				if (currentThumbnailPath != null && !currentThumbnailPath.isEmpty()) {
					imageService.deleteImage(currentThumbnailPath);
				}
				return imageService.saveImage(thumbnail, "product");
			} catch (IOException e) {
				throw new BusinessException("Lỗi khi xử lý ảnh sản phẩm");
			}
		}
		return currentThumbnailPath;
	}

}
