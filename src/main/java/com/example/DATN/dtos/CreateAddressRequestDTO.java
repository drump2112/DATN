package com.example.DATN.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAddressRequestDTO {
    private String specificAddress;
    private String communeCode;
    private String provinceCode;
    private Boolean isDefault;
}