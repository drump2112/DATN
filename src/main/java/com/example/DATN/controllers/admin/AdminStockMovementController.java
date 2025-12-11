package com.example.DATN.controllers.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.DATN.dtos.StockMovementDTO;
import com.example.DATN.services.StockMovementService;

@Controller
@RequestMapping("/admin/stock-management")
public class AdminStockMovementController {

    @Autowired
    private StockMovementService stockMovementService;

    /**
     * Trang chính quản lý lịch sử biến động kho
     */
    @GetMapping("/history")
    public String stockMovementsPage(Model model,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        System.out.println("🚀 AdminStockMovementController.stockMovementsPage() called");
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<StockMovementDTO> movements = stockMovementService.getAllStockMovements(pageable);

            model.addAttribute("movements", movements.getContent());
            model.addAttribute("currentPage", movements.getNumber());
            model.addAttribute("totalPages", movements.getTotalPages());
            model.addAttribute("totalItems", movements.getTotalElements());

            return "admin/stock-movements/list";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải dữ liệu: " + e.getMessage());
            return "admin/stock-movements/list";
        }
    }

    /**
     * Ajax endpoint để tải bảng với filter
     */
    @GetMapping("/history/table")
    public String stockMovementsTable(Model model,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    @RequestParam(required = false) String movementType,
                                    @RequestParam(required = false) String search) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<StockMovementDTO> movements;

            if (movementType != null && !movementType.isEmpty()) {
                movements = stockMovementService.getStockMovementsByType(movementType, pageable);
            } else {
                movements = stockMovementService.getAllStockMovements(pageable);
            }

            model.addAttribute("movements", movements.getContent());
            model.addAttribute("currentPage", movements.getNumber());
            model.addAttribute("totalPages", movements.getTotalPages());
            model.addAttribute("totalItems", movements.getTotalElements());

            return "admin/stock-movements/table :: table";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải dữ liệu: " + e.getMessage());
            model.addAttribute("movements", java.util.Collections.emptyList());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalItems", 0L);
            return "admin/stock-movements/table :: table";
        }
    }

    /**
     * Trang quản lý kho (nhập/xuất)
     */
    @GetMapping("/inventory")
    public String inventoryPage(Model model) {
        return "admin/inventory/manage";
    }

    /**
     * Test endpoint để debug routing
     */
    @GetMapping("/stock-movements-test")
    public String stockMovementsTest(Model model) {
        System.out.println("🔥 Test endpoint works!");
        model.addAttribute("message", "Controller routing hoạt động!");
        return "admin/stock-movements/list";
    }
}