package com.example.DATN.request;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryRequest {

    private Integer id;

    private String cateCode;

    private String name;

    private boolean isActive;
}
