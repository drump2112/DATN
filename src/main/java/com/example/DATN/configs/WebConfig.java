package com.example.DATN.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	private static final String PRODUCT_IMAGE_DIR = System.getProperty("user.dir") + "/uploads/";

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// Cấu hình để truy cập ảnh sản phẩm
		registry.addResourceHandler("/uploads/**")
				.addResourceLocations("file:" + PRODUCT_IMAGE_DIR);
	}

}
