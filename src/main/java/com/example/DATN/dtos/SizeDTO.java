package com.example.DATN.dtos;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SizeDTO {

	private Integer id;

	private String sizeCode;

	private String name;

    private Boolean isActive;

}
