package com.example.DATN.controllers.api;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.DATN.dtos.StockMovementDTO;
import com.example.DATN.services.StockMovementService;

@RestController
@RequestMapping("/api/stock-movements")
public class StockMovementController {

    @Autowired
    private StockMovementService stockMovementService;

    /**
     * Lấy lịch sử chuyển động kho của một product variant
     */
    @GetMapping("/variant/{variantId}")
    public ResponseEntity<List<StockMovementDTO>> getVariantHistory(@PathVariable Integer variantId) {
        try {
            List<StockMovementDTO> history = stockMovementService.getStockMovementHistory(variantId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Lấy lịch sử chuyển động kho với phân trang
     */
    @GetMapping("/variant/{variantId}/paged")
    public ResponseEntity<Page<StockMovementDTO>> getVariantHistoryPaged(
            @PathVariable Integer variantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<StockMovementDTO> history = stockMovementService.getStockMovementHistory(variantId, pageable);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Lấy tất cả chuyển động kho với phân trang
     */
    @GetMapping
    public ResponseEntity<Page<StockMovementDTO>> getAllMovements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<StockMovementDTO> movements = stockMovementService.getAllStockMovements(pageable);
            return ResponseEntity.ok(movements);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Lấy chuyển động kho theo loại
     */
    @GetMapping("/type/{movementType}")
    public ResponseEntity<Page<StockMovementDTO>> getMovementsByType(
            @PathVariable String movementType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<StockMovementDTO> movements = stockMovementService.getStockMovementsByType(movementType, pageable);
            return ResponseEntity.ok(movements);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cập nhật số lượng kho thủ công
     */
    @PostMapping("/update-stock")
    public ResponseEntity<Map<String, Object>> updateStock(@RequestBody Map<String, Object> request) {
        try {
            Integer variantId = (Integer) request.get("variantId");
            Integer newQuantity = (Integer) request.get("newQuantity");
            String note = (String) request.get("note");
            String createdBy = (String) request.get("createdBy");

            stockMovementService.updateStockWithMovement(variantId, newQuantity, "MANUAL", note, createdBy);

            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật kho thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Nhập kho
     */
    @PostMapping("/stock-in")
    public ResponseEntity<Map<String, Object>> stockIn(@RequestBody Map<String, Object> request) {
        try {
            Integer variantId = (Integer) request.get("variantId");
            Integer quantity = (Integer) request.get("quantity");
            String note = (String) request.get("note");
            String createdBy = (String) request.get("createdBy");

            stockMovementService.processStockIn(variantId, quantity, note, createdBy);

            return ResponseEntity.ok(Map.of("success", true, "message", "Nhập kho thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Xuất kho
     */
    @PostMapping("/stock-out")
    public ResponseEntity<Map<String, Object>> stockOut(@RequestBody Map<String, Object> request) {
        try {
            Integer variantId = (Integer) request.get("variantId");
            Integer quantity = (Integer) request.get("quantity");
            String note = (String) request.get("note");
            String createdBy = (String) request.get("createdBy");

            stockMovementService.processStockOut(variantId, quantity, note, createdBy);

            return ResponseEntity.ok(Map.of("success", true, "message", "Xuất kho thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Báo hàng hỏng
     */
    @PostMapping("/damage")
    public ResponseEntity<Map<String, Object>> reportDamage(@RequestBody Map<String, Object> request) {
        try {
            Integer variantId = (Integer) request.get("variantId");
            Integer quantity = (Integer) request.get("quantity");
            String note = (String) request.get("note");
            String createdBy = (String) request.get("createdBy");

            stockMovementService.processDamage(variantId, quantity, note, createdBy);

            return ResponseEntity.ok(Map.of("success", true, "message", "Báo hàng hỏng thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}