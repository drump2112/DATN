package com.example.DATN.services.impl;


import com.example.DATN.dtos.VoucherDTO;
import com.example.DATN.models.Voucher;
import com.example.DATN.repositories.VoucherRepository;
import com.example.DATN.request.VoucherRequest;
import com.example.DATN.services.VoucherService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class VoucherServiceImpl implements VoucherService {
   @Autowired
   private VoucherRepository voucherRepository;
   @Autowired
   private ModelMapper modelMapper;


   private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
   private static final Random RANDOM = new Random();


   @Override
   public Page<VoucherDTO> findAll(int page, int size) {
       Pageable pageable = PageRequest.of(page, size);
       System.out.println(pageable + "data");
       Page<Voucher> vouchers = voucherRepository.findAll(pageable);
       return vouchers.map(entity -> modelMapper.map(entity, VoucherDTO.class));
   }


   @Override
   public List<VoucherDTO> getVouchers(String keyword) {
       List<Voucher> vouchers;


       if (keyword != null && !keyword.isBlank()) {
           vouchers = voucherRepository.findByCodeContainingIgnoreCase(keyword);
       } else {
           vouchers = voucherRepository.findAll();
       }


       return vouchers.stream()
               .map(voucher -> VoucherDTO.builder()
                       .id(voucher.getId())
                       .name(voucher.getName())
                       .code(voucher.getCode())
                       .discountValue(voucher.getDiscountValue())
                       .discountType(voucher.getDiscountType())
                       .quantity(voucher.getQuantity())
                       .minOrderAmount(voucher.getMinOrderAmount())
                       .maxDiscountValue(voucher.getMaxDiscountValue())
                       .startDate(voucher.getStartDate())
                       .endDate(voucher.getEndDate())
                       .isActive(voucher.getIsActive())
                       .build()).collect(Collectors.toList());
   }


   @Override
   public boolean toggleStatus(Integer id) {
       Voucher voucher = voucherRepository.findById(id)
               .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá "));
       voucher.setIsActive(!voucher.getIsActive());
       voucherRepository.save(voucher);
       return voucher.getIsActive();
   }


   @Override
   public boolean addVoucherVoucher(VoucherRequest voucherRequest) {
       try {
           Voucher voucher = fromRequest(voucherRequest);
           voucher.setDiscountType(voucherRequest.getDiscountType()); // Nếu model có field type
           voucherRepository.save(voucher);
           return true;
       } catch (Exception e) {
           e.printStackTrace();
           return false;
       }
   }


   @Override
   public boolean addVoucherDotgiamgia(VoucherRequest voucherRequest) {
       try {
           Voucher voucher = fromRequest(voucherRequest);
           // Giả sử voucher là loại "Đợt Giảm Giá", có thể set thêm field type nếu model có
           voucher.setDiscountType(voucherRequest.getDiscountType()); // Nếu model có field type
           voucherRepository.save(voucher);
           return true;
       } catch (Exception e) {
           e.printStackTrace();
           return false;
       }
   }


   @Override
   public boolean updateVoucher(Integer id, VoucherRequest voucherRequest) {
       try {
           Optional<Voucher> optionalVoucher = voucherRepository.findById(id);
           if (optionalVoucher.isPresent()) {
               Voucher voucher = optionalVoucher.get();
               // Update fields từ request, giữ nguyên id và type
               modelMapper.map(voucherRequest, voucher); // Map các field chung
               voucher.setId(id);
               // Giữ nguyên code nếu không muốn thay đổi, hoặc update nếu request có
               if (voucherRequest.getCode() != null && !voucherRequest.getCode().trim().isEmpty()) {
                   voucher.setCode(voucherRequest.getCode());
               }
               voucher.setDiscountType(voucherRequest.getDiscountType()); // Giữ type
               voucherRepository.save(voucher);
               return true;
           }
           return false;
       } catch (Exception e) {
           e.printStackTrace();
           return false;
       }
   }


   @Override
   public boolean updateDotgiamgia(Integer id, VoucherRequest voucherRequest) {
       try {
           Optional<Voucher> optionalVoucher = voucherRepository.findById(id);
           if (optionalVoucher.isPresent()) {
               Voucher voucher = optionalVoucher.get();
               // Update fields từ request, giữ nguyên id và type
               modelMapper.map(voucherRequest, voucher); // Map các field chung
               voucher.setId(id);
               // Giữ nguyên code nếu không muốn thay đổi, hoặc update nếu request có
               if (voucherRequest.getCode() != null && !voucherRequest.getCode().trim().isEmpty()) {
                   voucher.setCode(voucherRequest.getCode());
               }
               voucher.setDiscountType(voucherRequest.getDiscountType()); // Giữ type
               voucherRepository.save(voucher);
               return true;
           }
           return false;
       } catch (Exception e) {
           e.printStackTrace();
           return false;
       }
   }


   @Override
   public Page<VoucherDTO> searchVoucher(String keyword, Boolean isActive, Pageable pageable) {
       // Gọi repo method với query riêng
       Page<Voucher> vouchers = voucherRepository.findBySearch(keyword, isActive, pageable);
       return vouchers.map(entity -> modelMapper.map(entity, VoucherDTO.class));
   }


   @Override
   public long countAll() {
       return voucherRepository.count();
   }


   public Voucher fromRequest(VoucherRequest voucherRequest) {


       Voucher.VoucherBuilder voucherBuilder = Voucher.builder()
               .id(voucherRequest.getId())
               .name(voucherRequest.getName())
//                nếu mã giảm giá null thì sẽ gọi hàm tự tạo mã
               .code(voucherRequest.getCode() != null && !voucherRequest.getCode().trim().isEmpty()
                       ? voucherRequest.getCode()
                       : generateDiscountCode())
               .discountValue(voucherRequest.getDiscountValue())
               .discountType(voucherRequest.getDiscountType())
               .quantity(voucherRequest.getQuantity())
               .minOrderAmount(voucherRequest.getMinOrderAmount())
               .maxDiscountValue(voucherRequest.getMaxDiscountValue())
               .startDate(voucherRequest.getStartDate() != null ? voucherRequest.getStartDate().atStartOfDay() : null)
               .endDate(voucherRequest.getEndDate() != null ? voucherRequest.getEndDate().atTime(23, 59, 59) : null)  // Kết thúc ngày: 23:59:59
               .isActive(voucherRequest.getIsActive());


       return voucherBuilder.build();
   }




   //     hàm tạo mã giảm giá tự động
   public String generateDiscountCode() {
       // Chọn ngẫu nhiên độ dài: 6 hoặc 8 ký tự (50% cơ hội mỗi loại)
       int length = RANDOM.nextBoolean() ? 6 : 8;


       StringBuilder code = new StringBuilder();
       for (int i = 0; i < length; i++) {
           int index = RANDOM.nextInt(CHARSET.length());
           code.append(CHARSET.charAt(index));
       }


       return code.toString();
   }
}

