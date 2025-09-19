// Controller Categories
package com.example.DATN.controllers.admin;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;




import com.example.DATN.dtos.CategoryDTO;
import com.example.DATN.models.Category;
import com.example.DATN.request.CategoryRequest;
import com.example.DATN.services.CategoryService;
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
@RequestMapping("admin/categories")
public class CategoryController {

    @Autowired
    CategoryService categoryService;

    @GetMapping("/")
    public String getAllCategies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Page<CategoryDTO> categiesDTOPage = categoryService.findAll(page, size);

        model.addAttribute("pageTitle", "Danh sách danh mục");
        model.addAttribute("categories", categiesDTOPage.getContent());
        model.addAttribute("currentPage", categiesDTOPage.getNumber());
        model.addAttribute("totalPages", categiesDTOPage.getTotalPages());
        model.addAttribute("totalItems", categiesDTOPage.getTotalElements());
        model.addAttribute("pageSize", categiesDTOPage.getSize());

        return "admin/categories/list";
    }

    @GetMapping("/select2")
    @ResponseBody
    public List<Map<String, Object>> getColorForSelect2(@RequestParam(required = false) String q) {
        List<CategoryDTO> colors = categoryService.getCategories(q);

        return colors.stream()
                .map(color -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", color.getId());
                    item.put("text", color.getName());
                    return item;
                })
                .collect(Collectors.toList());
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateSize(
            @PathVariable Integer id,
            @ModelAttribute CategoryRequest categoryRequest) {
        try {
            boolean result = categoryService.updateCategory(id, categoryRequest);
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
    public ResponseEntity<?> addSize(@ModelAttribute CategoryRequest CategoryRequest) {
        categoryService.addCategory(CategoryRequest);
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
        Page<CategoryDTO> usersPage = categoryService.searchCategory(keyword, isActive, pageable);

        model.addAttribute("categories", usersPage.getContent());
        model.addAttribute("currentPage", usersPage.getNumber());
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("totalItems", usersPage.getTotalElements());
        model.addAttribute("pageSize", usersPage.getSize());

        return "admin/categories/table :: table"; // Trả về fragment
    }


    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleCustomerStatus(@PathVariable Integer id) {
        boolean newStatus = categoryService.toggleStatus(id);
        String message = newStatus ? "Kích hoạt danh mục thành công" : "Đã khóa danh mục";
        return ResponseEntity.ok(Map.of("message", message));
    }

}

