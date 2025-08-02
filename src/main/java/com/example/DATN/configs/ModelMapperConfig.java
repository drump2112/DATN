package com.example.DATN.configs;

import com.example.DATN.dtos.ProductDTO;
import com.example.DATN.models.Product;

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

		return mapper;
	}
}
