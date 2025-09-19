package com.example.DATN.services;


import java.util.List;


import com.example.DATN.dtos.ColorDTO;


import com.example.DATN.request.ColorRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ColorService {


    Page<ColorDTO> findAll(int page, int size);


    List<ColorDTO> getColors(String keyword);


    boolean toggleStatus(Integer id);


    boolean addColor(ColorRequest colorRequet);


    boolean updateColor(Integer id, ColorRequest colorRequet);


    Page<ColorDTO> searchColor(String keyword, Boolean isActive, Pageable pageable);

}

