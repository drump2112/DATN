package com.example.DATN.dtos;

import lombok.Data;

@Data
public class CartItemDTO {

    private Integer variantId;

    private String variantCode;

    private Integer colorId;

    private String colorName;

    private Integer sizeId;

    private String sizeName;

    private String name;

    private Double price;

    private String image;

    private Integer quantity;

    private Integer maxQuantity;
}