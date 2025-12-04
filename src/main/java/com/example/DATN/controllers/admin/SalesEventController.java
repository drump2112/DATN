package com.example.DATN.controllers.admin;

import com.example.DATN.dtos.EventsDTO;
import com.example.DATN.request.EventsRequest;
import com.example.DATN.services.EventService;
import com.example.DATN.repositories.ProductVariantRepository;
import com.example.DATN.models.ProductVariant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/sales-event")
public class SalesEventController {

    @Autowired
    EventService eventService;

    @Autowired
    ProductVariantRepository productVariantRepository;

    @GetMapping("/")
    public String getAllSalesEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Page<EventsDTO> eventsPage = eventService.findAll(page, size);

        model.addAttribute("pageTitle", "Danh sách đợt giảm giá");
        model.addAttribute("events", eventsPage.getContent());
        model.addAttribute("currentPage", eventsPage.getNumber());
        model.addAttribute("totalPages", eventsPage.getTotalPages());
        model.addAttribute("totalItems", eventsPage.getTotalElements());
        model.addAttribute("pageSize", eventsPage.getSize());

        return "admin/voucher/listEvent";
    }

    @GetMapping("/table")
    public String getTableFragment(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Page<EventsDTO> eventsPage = eventService.findAll(page, size);

        model.addAttribute("pageTitle", "Danh sách đợt giảm giá");
        model.addAttribute("events", eventsPage.getContent());
        model.addAttribute("currentPage", eventsPage.getNumber());
        model.addAttribute("totalPages", eventsPage.getTotalPages());
        model.addAttribute("totalItems", eventsPage.getTotalElements());
        model.addAttribute("pageSize", eventsPage.getSize());

        return "admin/voucher/tableEvent :: table";
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSalesEvent(
            @PathVariable Integer id,
            @ModelAttribute EventsRequest eventsRequest) {
        try {
            boolean result = eventService.updateEvents(id, eventsRequest);
            if (result) {
                return ResponseEntity.ok(Map.of("message", "Cập nhật thành công"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Cập nhật thất bại"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventsDTO> getById(@PathVariable Integer id) {
        EventsDTO event = eventService.findById(id);
        if (event != null) {
            return ResponseEntity.ok(event);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/select2")
    @ResponseBody
    public java.util.List<Map<String, Object>> getEventsForSelect2(@RequestParam(required = false) String q) {
        java.util.List<EventsDTO> events = eventService.getEvents(q);

        return events.stream()
                .map(event -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", event.getId());
                    item.put("text", event.getName());
                    return item;
                })
                .collect(Collectors.toList());
    }

    @PostMapping("/add")
    public ResponseEntity<?> addSalesEvent(
            @ModelAttribute EventsRequest eventsRequest) {
        try {
            boolean success = eventService.addEvents(eventsRequest);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "Thêm thành công"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Thêm thất bại"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/search")
    public String searchSalesEvent(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<EventsDTO> events = eventService.searchEvents(keyword, isActive, pageable);

        model.addAttribute("events", events.getContent());
        model.addAttribute("currentPage", events.getNumber());
        model.addAttribute("totalPages", events.getTotalPages());
        model.addAttribute("totalItems", events.getTotalElements());
        model.addAttribute("pageSize", events.getSize());

        return "admin/sales-event/table :: table";
    }

    @GetMapping("/counts")
    @ResponseBody
    public long countSalesEvent(@RequestParam(required = false) String keyword) {
        return eventService.countAll();
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleSalesEventStatus(@PathVariable Integer id) {
        boolean newStatus = eventService.toggleStatus(id);
        String message = newStatus ? "Kích hoạt đợt giảm giá thành công" : "Đã khóa đợt giảm giá";
        return ResponseEntity.ok(Map.of("message", message));
    }

    @GetMapping("/product-variants")
    @ResponseBody
    public java.util.List<Map<String, Object>> getProductVariants(@RequestParam(required = false) String q) {
        java.util.List<ProductVariant> variants;
        if (q != null && !q.trim().isEmpty()) {
            // TODO: Implement search by q in repository
            variants = productVariantRepository.findAll(); // For now, get all
        } else {
            variants = productVariantRepository.findAll();
        }

        return variants.stream()
                .map(variant -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", variant.getId());
                    item.put("text", variant.getVariantCode() + " - " + variant.getProduct().getName());
                    return item;
                })
                .collect(java.util.stream.Collectors.toList());
    }
}