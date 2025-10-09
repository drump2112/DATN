package com.example.DATN.services;

import java.util.List;

import com.example.DATN.dtos.SizeDTO;

import com.example.DATN.request.SizeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SizeService {

    Page<SizeDTO> findAll(int page, int size);


    List<SizeDTO> getSizes(String keyword);


    boolean toggleStatus(Integer id);


    boolean addSize(SizeRequest sizerequet);


    boolean updateSize(Integer id, SizeRequest sizeRequest);


    Page<SizeDTO> searchSize(String keyword, Boolean isActive, Pageable pageable);

    long countAll();

}
