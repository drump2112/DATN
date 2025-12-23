package com.example.DATN.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {
    private Integer id;
    private String specificAddress;
    private String communeCode;
    private String communeName;
    private String provinceCode;
    private String provinceName;
    private String fullAddress;
    private Boolean isDefault;
    private Boolean isActive;
}