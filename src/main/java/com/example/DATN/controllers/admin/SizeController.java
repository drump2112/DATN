package com.example.DATN.controllers.admin;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import com.example.DATN.dtos.SizeDTO;
import com.example.DATN.request.SizeRequest;
import com.example.DATN.services.SizeService;
import com.example.DATN.repositories.SizeRepository;


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


@Controller
@RequestMapping("/admin/size")
public class SizeController {


    @Autowired
    private SizeService sizeService;

    @Autowired
    private SizeRepository sizeRepository;


    @GetMapping("/")
    public String getAllSize(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {


        Page<SizeDTO> sizePage = sizeService.findAll(page, size);


        model.addAttribute("pageTitle", "Danh sách kích cỡ");
        model.addAttribute("sizes", sizePage.getContent());
        model.addAttribute("currentPage", sizePage.getNumber());
        model.addAttribute("totalPages", sizePage.getTotalPages());
        model.addAttribute("totalItems", sizePage.getTotalElements());
        model.addAttribute("pageSize", sizePage.getSize());


        return "admin/size/list";

    }

    @GetMapping("/select2")
    @ResponseBody
    public List<Map<String, Object>> getSizeForSelect2(@RequestParam(required = false) String q) {
        List<SizeDTO> sizes = sizeService.getSizes(q);


        return sizes.stream()
                .map(size -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", size.getId());
                    item.put("text", size.getName());
                    return item;
                })
                .collect(Collectors.toList());
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateSize(
            @PathVariable Integer id,
            @ModelAttribute SizeRequest sizerequest) {
        try {
            boolean result = sizeService.updateSize(id, sizerequest);
            if (result) {
                return ResponseEntity.ok(Map.of("message", "Update Thành Công"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Thêm thất bại"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }


    @PostMapping("/add")
    public ResponseEntity<?> addSize(@ModelAttribute SizeRequest sizerequest) {
        sizeService.addSize(sizerequest);
        return ResponseEntity.ok(Map.of("message", "Thêm thành công"));
    }
    @GetMapping("/search")
    public String searchSize(


            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {


        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<SizeDTO> usersPage = sizeService.searchSize(keyword, isActive, pageable);


        model.addAttribute("sizes", usersPage.getContent());
        model.addAttribute("currentPage", usersPage.getNumber());
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("totalItems", usersPage.getTotalElements());
        model.addAttribute("pageSize", usersPage.getSize());


        return "admin/size/table :: table"; // Trả về fragment
    }

    @GetMapping("/counts")
	@ResponseBody
	public long countSize(@RequestParam(required = false) String keyword) {
		return sizeService.countAll();
	}

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleCustomerStatus(@PathVariable Integer id) {
        boolean newStatus = sizeService.toggleStatus(id);
        String message = newStatus ? "Kích hoạt kích thước thành công" : "Đã khóa kích thước";
        return ResponseEntity.ok(Map.of("message", message));
    }

    @GetMapping("/check-duplicate")
    @ResponseBody
    public ResponseEntity<?> checkSizeNameExists(
            @RequestParam String name,
            @RequestParam(required = false) Integer excludeId) {
        boolean exists;
        if (excludeId != null) {
            // Check cho update - loại trừ record hiện tại
            exists = sizeRepository.existsByNameAndIdNot(name.trim(), excludeId);
        } else {
            // Check cho add - kiểm tra tất cả
            exists = sizeRepository.existsByName(name.trim());
        }
        return ResponseEntity.ok(Map.of("exists", exists));
    }

}

