package com.example.DATN.controllers.admin;

import com.example.DATN.dtos.EventsDTO;
//import com.example.DATN.dtos.ProductToEventDTO;
import com.example.DATN.dtos.ProductVariantDTO;
//import com.example.DATN.dtos.SaleEventProductDTO;
import com.example.DATN.exception.BusinessException;
import com.example.DATN.models.SalesEvent;
import com.example.DATN.request.EventsRequest;
import com.example.DATN.services.EventService;
import com.example.DATN.services.ProductVariantService;
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
@RequestMapping("admin/event")
public class EventsController {

  @Autowired
  private EventService eventsService;

  @GetMapping("/")
  public String getListVoucher(
      @RequestParam(defaultValue = "0") int eventPage,
      @RequestParam(defaultValue = "5") int eventSize,
      Model model) {

    Page<EventsDTO> eventsDTOPage = eventsService.findAll(eventPage, eventSize);
    model.addAttribute("eventsPageTitle", "Danh sách Events");
    model.addAttribute("events", eventsDTOPage.getContent());
    model.addAttribute("eventCurrentPage", eventsDTOPage.getNumber());
    model.addAttribute("eventTotalPages", eventsDTOPage.getTotalPages());
    model.addAttribute("eventTotalItems", eventsDTOPage.getTotalElements());
    model.addAttribute("eventPageSize", eventsDTOPage.getSize());
    // tableEvent
    return "/admin/voucher/listEvent";
  }

  @GetMapping("/detail")
  public String getDetailVoucher() {
    return "/admin/voucher/eventDetail";

  }

  @GetMapping("/search")
  public String searchEvents(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "5") int size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Boolean isActive,
      Model model) {
    Pageable pageable = PageRequest.of(page, size);
    Page<EventsDTO> eventsDTOPage = eventsService.searchEvents(keyword, isActive, pageable);

    model.addAttribute("events", eventsDTOPage.getContent());
    model.addAttribute("eventCurrentPage", eventsDTOPage.getNumber());
    model.addAttribute("eventTotalPages", eventsDTOPage.getTotalPages());
    model.addAttribute("totalItems", eventsDTOPage.getTotalElements());
    model.addAttribute("pageSize", eventsDTOPage.getSize());
    model.addAttribute("keyword", keyword);
    model.addAttribute("isActive", isActive != null ? isActive.toString() : "");

    return "admin/voucher/tableEvent :: table"; // Fragment cho table + pagination
  }

  @PostMapping("/add")
  public ResponseEntity<Map<String, String>> addEvents(@ModelAttribute EventsRequest eventsRequest) {
    Map<String, String> response = new HashMap<>();
    try {
      boolean success = eventsService.addEvents(eventsRequest);
      if (success) {
        response.put("message", "Thêm thành công");
        return ResponseEntity.ok(response);
      } else {
        response.put("message", "Thêm Sự kiên thất bại!");
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
  public ResponseEntity<Map<String, String>> updateEvents(@PathVariable Integer id,
      @ModelAttribute EventsRequest eventsRequest) {
    Map<String, String> response = new HashMap<>();
    try {
      boolean success = eventsService.updateEvents(id, eventsRequest);
      if (success) {
        response.put("message", "Cập nhật Đợt giảm giá thành công!");
        return ResponseEntity.ok(response);
      } else {
        response.put("message", "Cập nhật Đợt giảm giá thất bại!");
        return ResponseEntity.badRequest().body(response);
      }
    } catch (Exception e) {
      response.put("message", "Cập nhật Đợt giảm giá thất bại: " + e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  @PutMapping("/{id}/toggle-status")
  public ResponseEntity<Map<String, String>> toggleStatus(@PathVariable Integer id) {
    boolean newStatus = eventsService.toggleStatus(id);
    String message = newStatus ? "Kích hoạt Sự kiện giảm giá thành công" : "Đã khóa Sự kiện giảm giá";
    return ResponseEntity.ok(Map.of("message", message));
  }

  @GetMapping("/counts")
  public ResponseEntity<Long> getCounts() {
    return ResponseEntity.ok(eventsService.countAll());
  }

  // @GetMapping("/detailEvent/{id}")
  // public String getDetailEvent(@PathVariable Integer id) {
  // SalesEvent salesEvent = eventsService.findById(id);
  //
  //
  // }

    @GetMapping("/detailEvent/{id}")
    public String getDetailEvent(@PathVariable Integer id,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "5") int size,
                                 Model model) {

        Pageable pageable = PageRequest.of(page, size);
//       lấy ra Eventdetail
        EventsDTO eventDetail = eventsService.findById(id);
//        Page<SaleEventProductDTO> saleEventProductDTOS = eventsService.getlistSaleEventProductvariant(id, pageable);
//        Page<ProductVariantDTO> productVariantPage = productVariantService.getAllProducts(page, size);

//        model.addAttribute("listProducts", productVariantPage.getContent());
        model.addAttribute("eventDetail", eventDetail);
//        model.addAttribute("saleEventProductDTOS", saleEventProductDTOS);
        return "admin/voucher/eventDetail";

    }
}
