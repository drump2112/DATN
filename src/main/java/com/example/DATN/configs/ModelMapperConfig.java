package com.example.DATN.configs;

import java.util.stream.Collectors;

import com.example.DATN.dtos.ProductDTO;
import com.example.DATN.dtos.ProductVariantDTO;
import com.example.DATN.models.Product;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.models.ProductVariantImage;
import com.example.DATN.repositories.ProductVariantImageRepository;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

	private final ProductVariantImageRepository imageRepository;

	public ModelMapperConfig(ProductVariantImageRepository imageRepository) {
		this.imageRepository = imageRepository;
	}

	@Bean
	public ModelMapper modelMapper() {
		ModelMapper mapper = new ModelMapper();

		// Mapping Product -> ProductDTO
		TypeMap<Product, ProductDTO> typeMap = mapper.createTypeMap(Product.class, ProductDTO.class);
		typeMap.setPostConverter(context -> {
			Product source = context.getSource();
			ProductDTO dest = context.getDestination();

			dest.setBrandName(source.getBrand() != null ? source.getBrand().getName() : null);
			dest.setCategoryName(source.getCategory() != null ? source.getCategory().getName() : null);

			return dest;
		});

		// Mapping ProductVariant -> ProductVariantDTO
		TypeMap<ProductVariant, ProductVariantDTO> variantTypeMap = mapper.createTypeMap(ProductVariant.class,
				ProductVariantDTO.class);
		variantTypeMap.setPostConverter(context -> {
			ProductVariant source = context.getSource();
			ProductVariantDTO dest = context.getDestination();

			if (source.getProduct() != null) {
				dest.setProductId(source.getProduct().getId());
				dest.setProductName(source.getProduct().getName());
				dest.setProductDescription(source.getProduct().getDescription());

				if (source.getProduct().getBrand() != null) {
					dest.setBrandId(source.getProduct().getBrand().getId());
					dest.setBrandName(source.getProduct().getBrand().getName());
				}
				if (source.getProduct().getCategory() != null) {
					dest.setCategoryId(source.getProduct().getCategory().getId());
					dest.setCategoryName(source.getProduct().getCategory().getName());
				}
			}

			if (source.getSize() != null) {
				dest.setSizeId(source.getSize().getId());
				dest.setSizeName(source.getSize().getName());
			}

			if (source.getColor() != null) {
				dest.setColorId(source.getColor().getId());
				dest.setColorName(source.getColor().getName());
			}

			// Lấy ảnh theo ProductId + ColorId
			if (source.getProduct() != null && source.getColor() != null) {
				var images = imageRepository.findByProductIdAndColorIdOrderBySortOrder(
						source.getProduct().getId(),
						source.getColor().getId());
				dest.setImageUrls(images.stream()
						.map(ProductVariantImage::getImageUrl)
						.collect(Collectors.toList()));
			}

			return dest;
		});

		return mapper;
	}
}
