package com.example.DATN.services;


import com.example.DATN.dtos.VoucherDTO;
import com.example.DATN.request.VoucherRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface VoucherService {

   Page<VoucherDTO> findAll(int page, int size);

   List<VoucherDTO> getVouchers(String keyword);

   boolean toggleStatus(Integer id);

   boolean addVoucherVoucher(VoucherRequest voucherRequest);

   boolean addVoucherDotgiamgia(VoucherRequest voucherRequest);

   boolean updateVoucher(Integer id, VoucherRequest voucherRequest);

   boolean updateDotgiamgia(Integer id, VoucherRequest voucherRequest);

   Page<VoucherDTO> searchVoucher(String keyword, Boolean isActive, Pageable pageable);

   long countAll();
}

