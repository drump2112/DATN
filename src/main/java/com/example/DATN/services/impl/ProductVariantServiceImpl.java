package com.example.DATN.services.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalTime;
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
		boolean exists = productVariantRepository.existsByProductIdAndColorIdAndSizeId(
				req.getProductId(), req.getColorId(), req.getSizeId());
		if (exists) {
			throw new BusinessException("Sản phẩm này đã tồn tại");
		}
		Optional<BigDecimal> existingPrice = productVariantRepository
				.findPriceByProductIdAndColorId(req.getProductId(), req.getColorId());

		BigDecimal price = existingPrice.orElseThrow(() -> new RuntimeException());

		if (existingPrice.isPresent() && existingPrice.get().compareTo(req.getPrice()) != 0) {
			throw new BusinessException("Sản phẩm đang có giá " + price + " VNĐ");
		}

		ProductVariant variant = fromRequest(req);
		productVariantRepository.save(variant);
		saveProductVariantImages(variant, Arrays.asList(req.getImages()));
		return true;
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

	public ProductVariant fromRequest(ProductVariantRequest req) {
		Product product = productRepository.findById(req.getProductId())
				.orElseThrow(() -> new BusinessException("Không tìm thấy sản phẩm"));

		Color color = colorRepository.findById(req.getColorId())
				.orElseThrow(() -> new BusinessException("Không tìm thấy màu sắc"));

		Size size = sizeRepository.findById(req.getSizeId())
				.orElseThrow(() -> new BusinessException("Không tìm thấy kích cỡ"));

		return ProductVariant.builder()
				.variantCode(LocalTime.now().toString())
				.product(product)
				.color(color)
				.size(size)
				.price(req.getPrice())
				.quantity(req.getQuantity())
				.sku(req.getSku())
				.status(req.getStatus())
				.build();
	}

	private void saveProductVariantImages(ProductVariant variant, List<MultipartFile> images) {
		if (images == null || images.isEmpty())
			return;

		int step = 0;
		for (MultipartFile file : images) {
			try {
				++step;
				String imagePath = imageService.saveImage(file, "product-variants");

				ProductVariantImage image = ProductVariantImage.builder()

						.productVariant(variant)
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

	// @Override
	// public List<ProductVariantDTO> getVariantsByProductId(Integer productId) {
	// List<ProductVariant> variants =
	// productVariantRepository.findByProductId(productId);
	// return variants.stream()
	// .map(this::toDTO)
	// .collect(Collectors.toList());
	// }

}
