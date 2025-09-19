package com.example.DATN.services;

import java.util.List;

import com.example.DATN.dtos.ProductDTO;
import com.example.DATN.models.Product;
import com.example.DATN.request.ProductRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

	Page<ProductDTO> getAllProducts(int page, int size);

	boolean toggleStatus(Integer id);

	boolean addProduct(ProductRequest productRequest);

	boolean updateProduct(Integer id, ProductRequest productRequest);

	Page<ProductDTO> searchProducts(String keyword, Boolean isActive, Pageable pageable);

	ProductDTO getProductDTOById(Integer id);

	List<ProductDTO> getProducts(String keyword);

	List<ProductDTO> getProductActive();

	ProductDTO getById(Integer id);

    public long countAll();

}
