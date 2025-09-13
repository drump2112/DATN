package com.example.DATN.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ColorRequest {


    private Integer id;


    private String colorCode;


    private String name;


    private boolean isActive;
}

