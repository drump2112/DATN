package com.example.DATN.services.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.example.DATN.dtos.ProductDTO;
import com.example.DATN.models.Brand;
import com.example.DATN.models.Category;
import com.example.DATN.models.Product;
import com.example.DATN.repositories.BrandRepository;
import com.example.DATN.repositories.CategoryRepository;
import com.example.DATN.repositories.ProductRepository;
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
			return dto;
		});
	}

	@Override
	public boolean toggleStatus(Integer id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
		product.setIsActive(!product.getIsActive());
		productRepository.save(product);
		return product.getIsActive();
	}

	@Override
	public ProductDTO getProductDTOById(Integer id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

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
			return dto;
		});

	}

	@Override
	public boolean addProduct(ProductRequest productRequest) {
		try {
			Product product = fromProductRequest(productRequest);
			productRepository.save(product);
			return true;

		} catch (Exception e) {
			throw new RuntimeException("Loi them san pham: " + e.getMessage(), e);
		}
	}

	@Override
	public boolean updateProduct(Integer id, ProductRequest productRequest) {
		try {
			// Tìm sản phẩm theo ID
			Product existingProduct = productRepository.findById(id)
					.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));

			// Lấy brand mới
			Brand brand = brandRepository.findById(productRequest.getBrandId())
					.orElseThrow(() -> new RuntimeException(
							"Không tìm thấy thương hiệu với ID: " + productRequest.getBrandId()));

			// Lấy category mới
			Category category = categoryRepository.findById(productRequest.getCategoryId())
					.orElseThrow(() -> new RuntimeException(
							"Không tìm thấy danh mục với ID: " + productRequest.getCategoryId()));

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
		} catch (Exception e) {
			throw new RuntimeException("Lỗi cập nhật sản phẩm: " + e.getMessage(), e);
		}
	}

	private Product fromProductRequest(ProductRequest req) {

		Product.ProductBuilder productBuilder = Product.builder()
				.name(req.getName())
				.description(req.getDescription())
				.createAt(new Date())
				.isActive(true); // default

		Brand brand = brandRepository.findById(req.getBrandId())
				.orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với id: " + req.getBrandId()));
		productBuilder.brand(brand);

		Category category = categoryRepository.findById(req.getCategoryId())
				.orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với id: " + req.getCategoryId()));
		productBuilder.category(category);

		String productCode = generateProductCode();
		productBuilder.productCode(productCode);

		if (req.getThumbnail() != null && !req.getThumbnail().isEmpty()) {
			String thumbnailPath = uploadThumbnail(req.getThumbnail());
			productBuilder.thumbnail(thumbnailPath);
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

	@Override
	public List<ProductDTO> getProductActive() {
		return productRepository.findByIsActive(true).stream()
				.map(product -> modelMapper.map(product, ProductDTO.class))
				.collect(Collectors.toList());
	}

	@Override
	public ProductDTO getById(Integer id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
		return modelMapper.map(product, ProductDTO.class);
	}

	private String uploadThumbnail(MultipartFile thumbnail) {
		try {
			return imageService.saveImage(thumbnail, "product");
		} catch (IOException e) {
			throw new RuntimeException("Lỗi khi lưu ảnh: " + e.getMessage(), e);
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
				throw new RuntimeException("Lỗi khi lưu ảnh: " + e.getMessage(), e);
			}
		}
		return currentThumbnailPath;
	}

}
