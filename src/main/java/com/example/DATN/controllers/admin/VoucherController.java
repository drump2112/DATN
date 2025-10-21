package com.example.DATN.controllers.admin;


import com.example.DATN.dtos.EventsDTO;
import com.example.DATN.dtos.VoucherDTO;
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
   private EventService eventsService;
   @Autowired
   private VoucherService voucherService;


   @GetMapping("/")
   public String getListVoucher(
           @RequestParam(defaultValue = "0") int eventPage,  // Pagination riêng cho Events
           @RequestParam(defaultValue = "5") int eventSize,
           @RequestParam(defaultValue = "0") int voucherPage,  // Pagination riêng cho Vouchers
           @RequestParam(defaultValue = "5") int voucherSize,
           Model model) {


       // Load Events
       Page<EventsDTO> eventsDTOPage = eventsService.findAll(eventPage, eventSize);
       model.addAttribute("eventsPageTitle", "Danh sách Events");
       model.addAttribute("events", eventsDTOPage.getContent());
       model.addAttribute("eventCurrentPage", eventsDTOPage.getNumber());
       model.addAttribute("eventTotalPages", eventsDTOPage.getTotalPages());
       model.addAttribute("eventTotalItems", eventsDTOPage.getTotalElements());
       model.addAttribute("eventPageSize", eventsDTOPage.getSize());


       // Load Vouchers
       Page<VoucherDTO> voucherDTOPage = voucherService.findAll(voucherPage, voucherSize);
       model.addAttribute("vouchersPageTitle", "Danh sách Vouchers");
       model.addAttribute("vouchers", voucherDTOPage.getContent());
       model.addAttribute("voucherCurrentPage", voucherDTOPage.getNumber());
       model.addAttribute("voucherTotalPages", voucherDTOPage.getTotalPages());
       model.addAttribute("voucherTotalItems", voucherDTOPage.getTotalElements());
       model.addAttribute("voucherPageSize", voucherDTOPage.getSize());


       return "admin/voucher/list";  // Return view chung
   }


   // GET /admin/voucher/search - Tìm kiếm voucher (trả HTML fragment cho table, sử dụng searchVoucher từ service)
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


       return "admin/voucher/tableVoucher :: table";  // Fragment cho table + pagination
   }




   @PostMapping("/add")
   public ResponseEntity<Map<String, String>> addVoucher(@ModelAttribute VoucherRequest voucherRequest) {
       Map<String, String> response = new HashMap<>();
       try {
           boolean success = voucherService.addVoucherVoucher(voucherRequest);
           if (success) {
               response.put("message", "Thêm voucher thành công!");
               return ResponseEntity.ok(response);
           } else {
               response.put("message", "Thêm voucher thất bại!");
               return ResponseEntity.badRequest().body(response);
           }
       } catch (Exception e) {
           response.put("message", "Thêm voucher thất bại: " + e.getMessage());
           return ResponseEntity.badRequest().body(response);
       }
   }


   // PUT /admin/voucher/{id} - Cập nhật voucher (sử dụng updateVoucher từ service, với VoucherRequest)
   @PutMapping("/{id}")
   public ResponseEntity<Map<String, String>> updateVoucher(@PathVariable Integer id, @ModelAttribute VoucherRequest voucherRequest) {
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


   // PUT /admin/voucher/{id}/toggle-status - Toggle trạng thái (sử dụng toggleStatus từ service)
   @PutMapping("/{id}/toggle-status")
   public ResponseEntity<Map<String, String>> toggleStatus(@PathVariable Integer id) {
       boolean newStatus = voucherService.toggleStatus(id);
       String message = newStatus ? "Kích hoạt kích mã giảm giá thành công" : "Đã khóa mã giảm giá";
       return ResponseEntity.ok(Map.of("message", message));
   }


   // GET /admin/voucher/counts - Đếm tổng số voucher (sử dụng countAll từ service)
   @GetMapping("/counts")
   public ResponseEntity<Long> getCounts() {
       return ResponseEntity.ok(voucherService.countAll());
   }
}



