package com.example.DATN.request;

import lombok.Data;

@Data
public class SizeRequest {
    private Integer id;

    private String sizeCode;

    private String name;

    private boolean isActive;
}
