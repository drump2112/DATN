package com.example.DATN.configs;

import java.util.stream.Collectors;

import com.example.DATN.dtos.ProductDTO;
import com.example.DATN.dtos.ProductVariantDTO;
import com.example.DATN.models.Product;
import com.example.DATN.models.ProductVariant;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

	@Bean
	public ModelMapper modelMapper() {
		ModelMapper mapper = new ModelMapper();

		TypeMap<Product, ProductDTO> typeMap = mapper.createTypeMap(Product.class, ProductDTO.class);

		typeMap.setPostConverter(context -> {
			Product source = context.getSource();
			ProductDTO dest = context.getDestination();

			dest.setBrandName(
					source.getBrand() != null ? source.getBrand().getName() : null);
			dest.setCategoryName(
					source.getCategory() != null ? source.getCategory().getName() : null);

			return dest;
		});

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
					dest.setBrandName(source.getProduct().getBrand().getName());
				}
				if (source.getProduct().getCategory() != null) {
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

			if (source.getImages() != null) {
				dest.setImageUrls(
						source.getImages().stream()
								.map(img -> img.getImageUrl()) // đúng getter
								.collect(Collectors.toList()));
			}

			return dest;
		});

		return mapper;
	}
}
