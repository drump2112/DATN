// Controlelr - color

package com.example.DATN.controllers.admin;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import com.example.DATN.dtos.ColorDTO;
import com.example.DATN.request.ColorRequest;
import com.example.DATN.services.ColorService;


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
@RequestMapping("admin/color")
public class ColorController {


    @Autowired
    ColorService colorService;

    @GetMapping("/")
    public String getAllColor(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Page<ColorDTO> colorsPage = colorService.findAll(page, size);

        model.addAttribute("pageTitle", "Danh sách màu sắc");
        model.addAttribute("colors", colorsPage.getContent());
        model.addAttribute("currentPage", colorsPage.getNumber());
        model.addAttribute("totalPages", colorsPage.getTotalPages());
        model.addAttribute("totalItems", colorsPage.getTotalElements());
        model.addAttribute("pageSize", colorsPage.getSize());

        return "admin/color/list";

    }

    @GetMapping("/select2")
    @ResponseBody
    public List<Map<String, Object>> getColorForSelect2(@RequestParam(required = false) String q) {
        List<ColorDTO> colors = colorService.getColors(q);


        return colors.stream()
                .map(color -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", color.getId());
                    item.put("text", color.getColorName());
                    return item;
                })
                .collect(Collectors.toList());
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateSize(
            @PathVariable Integer id,
            @ModelAttribute ColorRequest colorRequest) {
        try {
            boolean result = colorService.updateColor(id, colorRequest);
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
    public ResponseEntity<?> addSize(@ModelAttribute ColorRequest colorRequest) {
        colorService.addColor(colorRequest);
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
        Page<ColorDTO> usersPage = colorService.searchColor(keyword, isActive, pageable);


        model.addAttribute("colors", usersPage.getContent());
        model.addAttribute("currentPage", usersPage.getNumber());
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("totalItems", usersPage.getTotalElements());
        model.addAttribute("pageSize", usersPage.getSize());


        return "admin/color/table :: table"; // Trả về fragment
    }

  @GetMapping("/counts")
	@ResponseBody
	public long countColor(@RequestParam(required = false) String keyword) {
    return colorService.countAll();
	}


    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleColorStatus(@PathVariable Integer id) {
        boolean newStatus = colorService.toggleStatus(id);
        String message = newStatus ? "Kích hoạt màu sắc thành công" : "Đã khóa màu sắc";
        return ResponseEntity.ok(Map.of("message", message));
    }
}


