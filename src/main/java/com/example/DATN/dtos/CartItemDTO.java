package com.example.DATN.dtos;

import lombok.Data;

@Data
public class CartItemDTO {
    private Long variantId;
    private Long colorId;
    private String colorName;
    private Long sizeId;
    private String sizeName;
    private String name;
    private Double price;
    private String image;
    private Integer quantity;
}