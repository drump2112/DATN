package com.example.DATN.controllers.admin;

import com.example.DATN.dtos.VoucherDTO;
import com.example.DATN.exception.BusinessException;
import com.example.DATN.request.VoucherRequest;
import com.example.DATN.services.EventService;
import com.example.DATN.services.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("admin/voucher")
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    @GetMapping("/")
    public String getListVoucher(
            @RequestParam(defaultValue = "0") int voucherPage,
            @RequestParam(defaultValue = "5") int voucherSize,
            Model model) {

        Page<VoucherDTO> voucherDTOPage = voucherService.findAll(voucherPage, voucherSize);
        model.addAttribute("vouchersPageTitle", "Danh sách Vouchers");
        model.addAttribute("vouchers", voucherDTOPage.getContent());
        model.addAttribute("voucherCurrentPage", voucherDTOPage.getNumber());
        model.addAttribute("voucherTotalPages", voucherDTOPage.getTotalPages());
        model.addAttribute("voucherTotalItems", voucherDTOPage.getTotalElements());
        model.addAttribute("voucherPageSize", voucherDTOPage.getSize());

        return "admin/voucher/listVoucher";
    }

    @GetMapping("/search")
    public String searchVoucher(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<VoucherDTO> voucherDTOPage = voucherService.searchVoucher(keyword, isActive, pageable);

        model.addAttribute("vouchers", voucherDTOPage.getContent());
        model.addAttribute("voucherCurrentPage", voucherDTOPage.getNumber());
        model.addAttribute("voucherTotalPages", voucherDTOPage.getTotalPages());
        model.addAttribute("totalItems", voucherDTOPage.getTotalElements());
        model.addAttribute("pageSize", voucherDTOPage.getSize());
        model.addAttribute("keyword", keyword);
        model.addAttribute("isActive", isActive != null ? isActive.toString() : "");

        return "admin/voucher/tableVoucher :: table";
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> addVoucher(@ModelAttribute VoucherRequest voucherRequest) {
        Map<String, String> response = new HashMap<>();
        try {
            boolean success = voucherService.addVoucherVoucher(voucherRequest);
            if (success) {
                response.put("message", "Thêm thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Thêm voucher thất bại!");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (BusinessException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("message", "Lỗi không xác định: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateVoucher(@PathVariable Integer id,
            @ModelAttribute VoucherRequest voucherRequest) {
        Map<String, String> response = new HashMap<>();
        try {
            boolean success = voucherService.updateVoucher(id, voucherRequest);
            if (success) {
                response.put("message", "Cập nhật voucher thành công!");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Cập nhật voucher thất bại!");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("message", "Cập nhật voucher thất bại: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<Map<String, String>> toggleStatus(@PathVariable Integer id) {
        boolean newStatus = voucherService.toggleStatus(id);
        String message = newStatus ? "Kích hoạt mã giảm giá thành công" : "Đã khóa mã giảm giá";
        return ResponseEntity.ok(Map.of("message", message));
    }

    @GetMapping("/counts")
    public ResponseEntity<Long> getCounts() {
        return ResponseEntity.ok(voucherService.countAll());
    }
}
