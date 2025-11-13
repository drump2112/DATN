package com.example.DATN.services.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.DATN.dtos.ProductVariantDTO;
import com.example.DATN.exception.BusinessException;
import com.example.DATN.models.Color;
import com.example.DATN.models.Product;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.models.ProductVariantImage;
import com.example.DATN.models.Size;
import com.example.DATN.repositories.ColorRepository;
import com.example.DATN.repositories.ProductRepository;
import com.example.DATN.repositories.ProductVariantImageRepository;
import com.example.DATN.repositories.ProductVariantRepository;
import com.example.DATN.repositories.SizeRepository;
import com.example.DATN.request.ProductVariantRequest;
import com.example.DATN.request.ProductVariantUpdateRequest;
import com.example.DATN.services.ImageService;
import com.example.DATN.services.ProductVariantService;
import com.example.DATN.services.StockMovementService;
import com.example.DATN.specifications.ProductVariantSpecification;
import com.example.DATN.utils.AuthUtils;

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
	private ColorRepository colorRepository;

	@Autowired
	private SizeRepository sizeRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ImageService imageService;

	@Autowired
	private ProductVariantImageRepository variantImageRepository;

	@Autowired
	private StockMovementService stockMovementService;

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
		List<String> existingSizes = new ArrayList<>();
		for (Integer sizeId : req.getSizeIds()) {
			boolean exists = productVariantRepository.existsByProductIdAndColorIdAndSizeId(
					req.getProductId(), req.getColorId(), sizeId);
			if (exists) {
				existingSizes.add(sizeRepository.findById(sizeId).get().getName());
			}
		}
		if (!existingSizes.isEmpty()) {
			String message = existingSizes.size() == 1
					? "Size " + existingSizes.get(0) + " đã tồn tại"
					: "Các size " + String.join(", ", existingSizes) + " đã tồn tại";
			throw new BusinessException(message);
		}

		// Kiểm tra màu có tồn tại hay chưa trước khi lưu
		boolean colorExists = productVariantRepository.existsByProductIdAndColorId(req.getProductId(),
				req.getColorId());

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

		// Chỉ lưu ảnh nếu đây là màu mới
		if (!colorExists) {
			saveProductVariantImages(variants.get(0), req.getImages());
		}

		return true;
	}

	private List<ProductVariant> fromRequest(ProductVariantRequest req) {
		Product product = productRepository.findById(req.getProductId())
				.orElseThrow(() -> new BusinessException("Không tìm thấy sản phẩm"));

		Color color = colorRepository.findById(req.getColorId())
				.orElseThrow(() -> new BusinessException("Không tìm thấy màu sắc"));

		List<ProductVariant> variants = new ArrayList<>();

		// Lấy VariantCode lớn nhất hiện có
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

			// Tạo code duy nhất cho từng vòng lặp
			String variantCode = String.format("PV-%03d", nextNumber);
			nextNumber++;

			ProductVariant variant = ProductVariant.builder()
					.variantCode(variantCode)
					.product(product)
					.color(color)
					.size(size)
					.price(BigDecimal.valueOf(req.getPrice()))
					.quantity(quantity)
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
	public List<ProductVariantDTO> search(String keyword) {
		List<ProductVariant> list = productVariantRepository.searchByKeyword(keyword);

		return list.stream().map(pv -> ProductVariantDTO.builder()
				.id(pv.getId())
				.variantCode(pv.getVariantCode())
				.productId(pv.getProduct() != null ? pv.getProduct().getId() : null)
				.productName(pv.getProduct() != null ? pv.getProduct().getName() : null)
				.colorId(pv.getColor() != null ? pv.getColor().getId() : null)
				.colorName(pv.getColor() != null ? pv.getColor().getName() : null)
				.sizeId(pv.getSize() != null ? pv.getSize().getId() : null)
				.sizeName(pv.getSize() != null ? pv.getSize().getName() : null)
				.price(pv.getPrice())
				.quantity(pv.getQuantity())
				.status(pv.getStatus())
				.imageUrls(
						pv.getProduct() != null && pv.getProduct().getProductVariantImages() != null
								? pv.getProduct().getProductVariantImages().stream()
										.filter(img -> pv.getColor() != null && img.getColor().getId().equals(pv.getColor().getId()))
										.map(ProductVariantImage::getImageUrl)
										.collect(Collectors.toList())
								: new ArrayList<>())
				.build()).collect(Collectors.toList());
	}

	@Override
	public List<ProductVariantDTO> findAllActive() {
		List<ProductVariant> list = productVariantRepository.findByStatusTrue();

		return list.stream().map(pv -> ProductVariantDTO.builder()
				.id(pv.getId())
				.variantCode(pv.getVariantCode())
				.productId(pv.getProduct() != null ? pv.getProduct().getId() : null)
				.productName(pv.getProduct() != null ? pv.getProduct().getName() : null)
				.productDescription(pv.getProduct() != null ? pv.getProduct().getDescription() : null)
				.brandId(
						pv.getProduct() != null && pv.getProduct().getBrand() != null ? pv.getProduct().getBrand().getId() : null)
				.brandName(
						pv.getProduct() != null && pv.getProduct().getBrand() != null ? pv.getProduct().getBrand().getName() : null)
				.categoryId(
						pv.getProduct() != null && pv.getProduct().getCategory() != null ? pv.getProduct().getCategory().getId()
								: null)
				.categoryName(
						pv.getProduct() != null && pv.getProduct().getCategory() != null ? pv.getProduct().getCategory().getName()
								: null)
				.colorId(pv.getColor() != null ? pv.getColor().getId() : null)
				.colorName(pv.getColor() != null ? pv.getColor().getName() : null)
				.sizeId(pv.getSize() != null ? pv.getSize().getId() : null)
				.sizeName(pv.getSize() != null ? pv.getSize().getName() : null)
				.price(pv.getPrice())
				.quantity(pv.getQuantity())
				.status(pv.getStatus())
				.imageUrls(
						pv.getProduct() != null && pv.getProduct().getProductVariantImages() != null
								? pv.getProduct().getProductVariantImages().stream()
										.filter(img -> pv.getColor() != null && img.getColor().getId().equals(pv.getColor().getId()))
										.map(ProductVariantImage::getImageUrl)
										.collect(Collectors.toList())
								: new ArrayList<>())
				.build()).collect(Collectors.toList());
	}

	@Override
	public Page<ProductVariantDTO> searchProductVariants(
			String keyword,
			Integer colorId,
			Integer sizeId,
			Integer cateId,
			Integer brandId,
			Boolean status,
			Pageable pageable) {

		Specification<ProductVariant> spec = Specification
				.where(ProductVariantSpecification.containsKeyword(keyword))
				.and(ProductVariantSpecification.hasColor(colorId))
				.and(ProductVariantSpecification.hasSize(sizeId))
				.and(ProductVariantSpecification.hasCategory(cateId))
				.and(ProductVariantSpecification.hasBrand(brandId))
				.and(ProductVariantSpecification.hasStatus(status));

		Page<ProductVariant> pageResult = productVariantRepository.findAll(spec, pageable);

		return pageResult.map(this::mapToDTO);
	}

	@Override
	public Page<ProductVariantDTO> searchProductVariantsInventory(
			String keyword,
			Integer colorId,
			Integer sizeId,
			Integer cateId,
			Pageable pageable) {

		Specification<ProductVariant> spec = Specification
				.where(ProductVariantSpecification.containsKeyword(keyword))
				.and(ProductVariantSpecification.hasColor(colorId))
				.and(ProductVariantSpecification.hasSize(sizeId))
				.and(ProductVariantSpecification.hasCategory(cateId));

		Page<ProductVariant> pageResult = productVariantRepository.findAll(spec, pageable);

		return pageResult.map(this::mapToDTO);
	}

	@Override
	public Page<ProductVariantDTO> getVariantsByProductId(int size, int page, int id) {
		Pageable pageable = PageRequest.of(page, size);
		Page<ProductVariant> productVariant = productVariantRepository.findProductVariantsByProduct_Id(id, pageable);
		return productVariant.map(entity -> modelMapper.map(entity, ProductVariantDTO.class));
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
				.map(this::mapToProductVariantDTO)
				.collect(Collectors.toList());
	}

	@Override
	public ProductVariantDTO findById(Integer id) {
		ProductVariant variant = productVariantRepository.findDetailById(id)
				.orElseThrow(() -> new RuntimeException("ProductVariant not found with id: " + id));

		return modelMapper.map(variant, ProductVariantDTO.class);
	}

	@Override
	public void updateProductVariant(Integer id, ProductVariantUpdateRequest request) {
		ProductVariant variant = productVariantRepository.findById(id)
				.orElseThrow(() -> new BusinessException("Không tìm thấy sản phẩm"));

		// Lưu số lượng cũ để ghi stock movement
		Integer oldQuantity = variant.getQuantity();

		if (request.getPrice() != null) {
			variant.setPrice(BigDecimal.valueOf(request.getPrice()));
		}

		if (request.getQuantity() != null) {
			variant.setQuantity(request.getQuantity());
		}

		productVariantRepository.save(variant);

		// Ghi lại stock movement nếu số lượng thay đổi
		if (request.getQuantity() != null && !request.getQuantity().equals(oldQuantity)) {
			String actor = AuthUtils.getCurrentFullName();
			stockMovementService.processManualUpdate(
				variant.getId(),
				oldQuantity,
				request.getQuantity(),
				"Cập nhật số lượng thủ công từ admin",
				actor
			);
		}

		if (request.getImages() != null && !request.getImages().isEmpty()) {
			List<ProductVariantImage> oldImages = variantImageRepository.findByProductIdAndColorIdOrderBySortOrder(
					variant.getProduct().getId(),
					variant.getColor().getId());

			for (ProductVariantImage old : oldImages) {
				try {
					imageService.deleteImage(old.getImageUrl()); // xoá file vật lý
				} catch (IOException e) {
					System.err.println("Không thể xóa file: " + old.getImageUrl());
				}
			}
			variantImageRepository.deleteAll(oldImages);

			// Lưu ảnh mới
			int step = 0;
			for (MultipartFile file : request.getImages()) {
				try {
					++step;
					String imagePath = imageService.saveImage(file, "product-variants");

					ProductVariantImage image = ProductVariantImage.builder()
							.product(variant.getProduct())
							.color(variant.getColor())
							.imageUrl(imagePath)
							.sortOrder(step)
							.build();

					variantImageRepository.save(image);
				} catch (IOException e) {
					throw new BusinessException("Lỗi khi lưu ảnh mới");
				}
			}
		}
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

	@Override
	public List<ProductVariantDTO> getBestSellingVariants(int limit) {
		Pageable pageable = PageRequest.of(0, limit);
		List<Object[]> results = productVariantRepository.findBestSellingVariants(pageable);

		return results.stream()
				.map(result -> {
					Integer variantId = (Integer) result[0];
					ProductVariant variant = productVariantRepository.findDetailById(variantId).orElse(null);

					if (variant == null || !variant.getStatus()) {
						return null;
					}

					return mapToProductVariantDTO(variant);
				})
				.filter(dto -> dto != null)
				.collect(Collectors.toList());
	}

	@Override
	public List<ProductVariantDTO> getNewestVariants(int limit) {
		Pageable pageable = PageRequest.of(0, limit);
		List<ProductVariant> variants = productVariantRepository.findNewestVariants(pageable);

		return variants.stream()
				.map(this::mapToProductVariantDTO)
				.collect(Collectors.toList());
	}

	private ProductVariantDTO mapToProductVariantDTO(ProductVariant variant) {
		ProductVariantDTO dto = modelMapper.map(variant, ProductVariantDTO.class);

		if (variant.getProduct() != null) {
			dto.setProductId(variant.getProduct().getId());
			dto.setProductName(variant.getProduct().getName());
			dto.setProductDescription(variant.getProduct().getDescription());

			if (variant.getProduct().getBrand() != null) {
				dto.setBrandId(variant.getProduct().getBrand().getId());
				dto.setBrandName(variant.getProduct().getBrand().getName());
			}

			if (variant.getProduct().getCategory() != null) {
				dto.setCategoryId(variant.getProduct().getCategory().getId());
				dto.setCategoryName(variant.getProduct().getCategory().getName());
			}
		}

		if (variant.getSize() != null) {
			dto.setSizeId(variant.getSize().getId());
			dto.setSizeName(variant.getSize().getName());
		}

		if (variant.getColor() != null) {
			dto.setColorId(variant.getColor().getId());
			dto.setColorName(variant.getColor().getName());

			// Lấy danh sách ảnh từ ProductVariantImage theo productId và colorId
			if (variant.getProduct() != null) {
				List<ProductVariantImage> images = variantImageRepository
						.findByProductIdAndColorIdOrderBySortOrder(
								variant.getProduct().getId(),
								variant.getColor().getId());

				if (images != null && !images.isEmpty()) {
					dto.setImageUrls(images.stream()
							.map(ProductVariantImage::getImageUrl)
							.collect(Collectors.toList()));
				}
			}
		}

		return dto;
	}

}
