package com.example.DATN.services.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.spi.LocaleServiceProvider;
import java.util.stream.Collectors;

import com.example.DATN.dtos.ProductVariantDTO;
import com.example.DATN.exception.BusinessException;
import com.example.DATN.models.Color;
import com.example.DATN.models.Product;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.models.ProductVariantImage;
import com.example.DATN.models.Size;
import com.example.DATN.repositories.ColorRepoSitory;
import com.example.DATN.repositories.ProductRepository;
import com.example.DATN.repositories.ProductVariantImageRepository;
import com.example.DATN.repositories.ProductVariantRepository;
import com.example.DATN.repositories.SizeRepository;
import com.example.DATN.request.ProductVariantRequest;
import com.example.DATN.services.ImageService;
import com.example.DATN.services.ProductVariantService;
import com.example.DATN.specifications.ProductVariantSpecification;

import org.hibernate.metamodel.internal.RuntimeMetamodelsImpl;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductVariantServiceImpl implements ProductVariantService {

	@Autowired
	private ProductVariantRepository productVariantRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private ColorRepoSitory colorRepository;

	@Autowired
	private SizeRepository sizeRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ImageService imageService;

	@Autowired
	private ProductVariantImageRepository variantImageRepository;

	@Override
	public Page<ProductVariantDTO> getAllProducts(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);

		Page<ProductVariant> productVariant = productVariantRepository.findAll(pageable);

		return productVariant.map(entity -> modelMapper.map(entity, ProductVariantDTO.class));
	}

	@Override
	public boolean toggleStatus(Integer id) {
		ProductVariant productVariant = productVariantRepository.findById(id)
				.orElseThrow(() -> new BusinessException("Không tìm thây sản phẩm"));
		productVariant.setStatus(!productVariant.getStatus());
		productVariantRepository.save(productVariant);
		return true;
	}

	@Override
	public boolean addProductVariant(ProductVariantRequest req) {
		// Kiểm tra biến thể đã tồn tại
		for (Integer sizeId : req.getSizeIds()) {
			boolean exists = productVariantRepository.existsByProductIdAndColorIdAndSizeId(
					req.getProductId(), req.getColorId(), sizeId);
			if (exists) {
				throw new BusinessException("Sản phẩm size " + sizeId + " đã tồn tại");
			}
		}

		// Kiểm tra giá chung
		Optional<BigDecimal> existingPrice = productVariantRepository
				.findPriceByProductIdAndColorId(req.getProductId(), req.getColorId());

		BigDecimal price = existingPrice.orElse(BigDecimal.valueOf(req.getPrice()));
		if (existingPrice.isPresent() && existingPrice.get().compareTo(BigDecimal.valueOf(req.getPrice())) != 0) {
			throw new BusinessException("Sản phẩm đang có giá " + price + " VNĐ");
		}

		// Tạo danh sách biến thể
		List<ProductVariant> variants = fromRequest(req);
		productVariantRepository.saveAll(variants);

		// Lưu ảnh (chung cho 1 màu)
		saveProductVariantImages(variants.get(0), req.getImages());

		return true;
	}

	private List<ProductVariant> fromRequest(ProductVariantRequest req) {
		Product product = productRepository.findById(req.getProductId())
				.orElseThrow(() -> new BusinessException("Không tìm thấy sản phẩm"));

		Color color = colorRepository.findById(req.getColorId())
				.orElseThrow(() -> new BusinessException("Không tìm thấy màu sắc"));

		List<ProductVariant> variants = new ArrayList<>();

		// ✅ Lấy VariantCode lớn nhất hiện có
		String maxCode = productVariantRepository.findMaxVariantCode();
		int nextNumber = 1;
		if (maxCode != null) {
			try {
				String numberPart = maxCode.split("-")[1];
				nextNumber = Integer.parseInt(numberPart) + 1;
			} catch (Exception e) {
				throw new BusinessException("VariantCode trong DB không đúng format PV-XXX");
			}
		}

		for (Integer sizeId : req.getSizeIds()) {
			Size size = sizeRepository.findById(sizeId)
					.orElseThrow(() -> new BusinessException("Không tìm thấy kích cỡ ID = " + sizeId));

			Integer quantity = req.getQuantities().get(sizeId);
			String sku = req.getSkus().get(sizeId);

			if (quantity == null || sku == null) {
				throw new BusinessException("Số lượng hoặc SKU không được cung cấp cho kích cỡ " + sizeId);
			}

			// ✅ Tạo code duy nhất cho từng vòng lặp
			String variantCode = String.format("PV-%03d", nextNumber);
			nextNumber++;

			ProductVariant variant = ProductVariant.builder()
					.variantCode(variantCode)
					.product(product)
					.color(color)
					.size(size)
					.price(BigDecimal.valueOf(req.getPrice()))
					.quantity(quantity)
					.sku(sku)
					.status(req.getStatus())
					.build();

			variants.add(variant);
		}

		return variants;
	}

	@Override
	public boolean updateProductVariant(Integer id) {
		ProductVariant productVariant = productVariantRepository.findById(id)
				.orElseThrow(() -> new BusinessException("Không tìm thây sản phẩm"));
		productVariant.setStatus(!productVariant.getStatus());
		productVariantRepository.save(productVariant);
		return true;
	}

	@Override
	public Page<ProductVariantDTO> searchProductVariants(
			String keyword,
			Integer colorId,
			Integer sizeId,
			Integer cateId,
			Boolean status,
			Pageable pageable) {

		Specification<ProductVariant> spec = Specification
				.where(ProductVariantSpecification.containsKeyword(keyword))
				.and(ProductVariantSpecification.hasColor(colorId))
				.and(ProductVariantSpecification.hasSize(sizeId))
				.and(ProductVariantSpecification.hasCategory(cateId))
				.and(ProductVariantSpecification.hasStatus(status));

		Page<ProductVariant> pageResult = productVariantRepository.findAll(spec, pageable);

		return pageResult.map(this::mapToDTO);
	}

	private void saveProductVariantImages(ProductVariant variant, List<MultipartFile> images) {
		if (images == null || images.isEmpty()) {
			return;
		}

		// Lấy product và color từ variant
		Product product = variant.getProduct();
		Color color = variant.getColor();

		if (product == null || color == null) {
			throw new BusinessException("Product hoặc Color lỗi !");
		}

		Integer productId = product.getId();
		Integer colorId = color.getId();

		// Kiểm tra nếu đã tồn tại ảnh cho cặp product + color thì bỏ qua
		List<ProductVariantImage> existing = variantImageRepository
				.findByProductIdAndColorIdOrderBySortOrder(productId, colorId);
		if (!existing.isEmpty()) {
			return;
		}

		int step = 0;
		for (MultipartFile file : images) {
			try {
				++step;
				String imagePath = imageService.saveImage(file, "product-variants");

				ProductVariantImage image = ProductVariantImage.builder()
						.product(product)
						.color(color)
						.imageUrl(imagePath)
						.sortOrder(step)
						.build();

				variantImageRepository.save(image);
			} catch (IOException e) {
				throw new BusinessException("Lỗi khi lưu ảnh");
			}
		}
	}

	private ProductVariantDTO mapToDTO(ProductVariant entity) {
		ProductVariantDTO dto = modelMapper.map(entity, ProductVariantDTO.class);

		if (entity.getProduct() != null) {
			dto.setProductName(entity.getProduct().getName());

			if (entity.getProduct().getBrand() != null) {
				dto.setBrandName(entity.getProduct().getBrand().getName());
			}
			if (entity.getProduct().getCategory() != null) {
				dto.setCategoryName(entity.getProduct().getCategory().getName());
			}
		}
		if (entity.getColor() != null) {
			dto.setColorName(entity.getColor().getName());
		}
		if (entity.getSize() != null) {
			dto.setSizeName(entity.getSize().getName());
		}

		return dto;
	}

	@Override
	public long countAll() {
		return productVariantRepository.count();
	}

	@Override
	public List<ProductVariantDTO> getVariantsByProductId(Integer productId) {
		List<ProductVariant> variants = productVariantRepository.findByProductId(productId);
		return variants.stream()
				.map(variant -> modelMapper.map(variant, ProductVariantDTO.class))
				.collect(Collectors.toList());
	}

	@Override
	public ProductVariantDTO findById(Integer id) {
		ProductVariant variant = productVariantRepository.findDetailById(id)
				.orElseThrow(() -> new RuntimeException("ProductVariant not found with id: " + id));

		return modelMapper.map(variant, ProductVariantDTO.class);
	}

	private String generateVariantCode() {
		String prefix = "PV";

		// Đếm tổng số variant hiện có
		long count = productVariantRepository.count();

		String variantCode;
		int suffix = (int) count + 1;

		do {
			variantCode = String.format("%s-%03d", prefix, suffix);
			suffix++;
		} while (productVariantRepository.existsByVariantCode(variantCode));

		return variantCode;
	}

}
