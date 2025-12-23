package com.example.DATN.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BrandDTO {
	private Integer id;

	private String brandCode;

	private String name;

	private String logoUrl;

    private Boolean isActive;

}
