package com.example.DATN.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.DATN.dtos.StockMovementDTO;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.models.StockMovement;
import com.example.DATN.repositories.ProductVariantRepository;
import com.example.DATN.repositories.StockMovementRepository;
import com.example.DATN.services.StockMovementService;

@Service
@Transactional
public class StockMovementServiceImpl implements StockMovementService {

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Override
    public StockMovement recordStockMovement(Integer productVariantId, Integer quantity,
                                           String movementType, String note, String createdBy) {
        ProductVariant variant = productVariantRepository.findById(productVariantId)
            .orElseThrow(() -> new RuntimeException("Product variant not found: " + productVariantId));

        return recordStockMovement(variant, quantity, movementType, note, createdBy);
    }

    @Override
    public StockMovement recordStockMovement(ProductVariant productVariant, Integer quantity,
                                           String movementType, String note, String createdBy) {
        StockMovement movement = StockMovement.builder()
            .productVariant(productVariant)
            .quantity(quantity)
            .movementType(movementType)
            .note(note)
            .createdAt(LocalDateTime.now())
            .createdBy(createdBy)
            .build();

        return stockMovementRepository.save(movement);
    }

    @Override
    public List<StockMovementDTO> getStockMovementHistory(Integer productVariantId) {
        List<StockMovement> movements = stockMovementRepository
            .findByProductVariantIdOrderByCreatedAtDesc(productVariantId);

        return movements.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public Page<StockMovementDTO> getStockMovementHistory(Integer productVariantId, Pageable pageable) {
        Page<StockMovement> movements = stockMovementRepository
            .findByProductVariantIdOrderByCreatedAtDesc(productVariantId, pageable);

        return movements.map(this::convertToDTO);
    }

    @Override
    public Page<StockMovementDTO> getAllStockMovements(Pageable pageable) {
        Page<StockMovement> movements = stockMovementRepository.findAll(pageable);
        return movements.map(this::convertToDTO);
    }

    @Override
    public Page<StockMovementDTO> getStockMovementsByType(String movementType, Pageable pageable) {
        Page<StockMovement> movements = stockMovementRepository
            .findByMovementTypeOrderByCreatedAtDesc(movementType, pageable);

        return movements.map(this::convertToDTO);
    }

    @Override
    public void updateStockWithMovement(Integer productVariantId, Integer newQuantity,
                                      String movementType, String note, String createdBy) {
        ProductVariant variant = productVariantRepository.findById(productVariantId)
            .orElseThrow(() -> new RuntimeException("Product variant not found: " + productVariantId));

        Integer oldQuantity = variant.getQuantity();
        Integer changeAmount = newQuantity - oldQuantity;

        // Cập nhật số lượng
        variant.setQuantity(newQuantity);
        productVariantRepository.save(variant);

        // Ghi lại chuyển động
        String detailNote = String.format("Cập nhật từ %d -> %d. %s", oldQuantity, newQuantity,
                                        note != null ? note : "");
        recordStockMovement(variant, Math.abs(changeAmount), movementType, detailNote, createdBy);
    }

    @Override
    public void processSale(Integer productVariantId, Integer quantity, String orderCode, String createdBy) {
        ProductVariant variant = productVariantRepository.findById(productVariantId)
            .orElseThrow(() -> new RuntimeException("Product variant not found: " + productVariantId));

        if (variant.getQuantity() < quantity) {
            throw new RuntimeException("Không đủ hàng trong kho. Có: " + variant.getQuantity() +
                                     ", cần: " + quantity);
        }

        // Trừ kho
        variant.setQuantity(variant.getQuantity() - quantity);
        productVariantRepository.save(variant);

        // Ghi lại chuyển động
        String note = "Bán hàng - Đơn: " + orderCode;
        recordStockMovement(variant, quantity, "SALE", note, createdBy);
    }

    @Override
    public void processReturn(Integer productVariantId, Integer quantity, String orderCode, String createdBy) {
        ProductVariant variant = productVariantRepository.findById(productVariantId)
            .orElseThrow(() -> new RuntimeException("Product variant not found: " + productVariantId));

        // Cộng kho
        variant.setQuantity(variant.getQuantity() + quantity);
        productVariantRepository.save(variant);

        // Ghi lại chuyển động
        String note = "Hoàn trả - Đơn: " + orderCode;
        recordStockMovement(variant, quantity, "RETURN", note, createdBy);
    }

    @Override
    public void processStockIn(Integer productVariantId, Integer quantity, String note, String createdBy) {
        ProductVariant variant = productVariantRepository.findById(productVariantId)
            .orElseThrow(() -> new RuntimeException("Product variant not found: " + productVariantId));

        // Cộng kho
        variant.setQuantity(variant.getQuantity() + quantity);
        productVariantRepository.save(variant);

        // Ghi lại chuyển động
        recordStockMovement(variant, quantity, "IN", note, createdBy);
    }

    @Override
    public void processStockOut(Integer productVariantId, Integer quantity, String note, String createdBy) {
        ProductVariant variant = productVariantRepository.findById(productVariantId)
            .orElseThrow(() -> new RuntimeException("Product variant not found: " + productVariantId));

        if (variant.getQuantity() < quantity) {
            throw new RuntimeException("Không đủ hàng trong kho. Có: " + variant.getQuantity() +
                                     ", cần: " + quantity);
        }

        // Trừ kho
        variant.setQuantity(variant.getQuantity() - quantity);
        productVariantRepository.save(variant);

        // Ghi lại chuyển động
        recordStockMovement(variant, quantity, "OUT", note, createdBy);
    }

    @Override
    public void processDamage(Integer productVariantId, Integer quantity, String note, String createdBy) {
        ProductVariant variant = productVariantRepository.findById(productVariantId)
            .orElseThrow(() -> new RuntimeException("Product variant not found: " + productVariantId));

        if (variant.getQuantity() < quantity) {
            throw new RuntimeException("Không đủ hàng trong kho. Có: " + variant.getQuantity() +
                                     ", cần: " + quantity);
        }

        // Trừ kho
        variant.setQuantity(variant.getQuantity() - quantity);
        productVariantRepository.save(variant);

        // Ghi lại chuyển động
        recordStockMovement(variant, quantity, "DAMAGE", note, createdBy);
    }

    private StockMovementDTO convertToDTO(StockMovement movement) {
        ProductVariant variant = movement.getProductVariant();

        return StockMovementDTO.builder()
            .id(movement.getId())
            .productVariantId(variant.getId())
            .variantCode(variant.getVariantCode())
            .productName(variant.getProduct().getName())
            .colorName(variant.getColor().getName())
            .sizeName(variant.getSize().getName())
            .quantity(movement.getQuantity())
            .movementType(movement.getMovementType())
            .movementTypeDisplay(getMovementTypeText(movement.getMovementType()))
            .note(movement.getNote())
            .createdAt(movement.getCreatedAt())
            .createdBy(movement.getCreatedBy())
            .currentStock(variant.getQuantity())
            .build();
    }

    @Override
    public void processManualUpdate(Integer productVariantId, Integer oldQuantity, Integer newQuantity, String note, String createdBy) {
        ProductVariant variant = productVariantRepository.findById(productVariantId)
            .orElseThrow(() -> new RuntimeException("Product variant not found: " + productVariantId));

        // Tính toán sự thay đổi
        int quantityChange = newQuantity - oldQuantity;

        if (quantityChange != 0) {
            // Ghi lại chuyển động với số lượng thay đổi (có thể âm hoặc dương)
            String movementType = quantityChange > 0 ? "MANUAL_IN" : "MANUAL_OUT";
            recordStockMovement(variant, Math.abs(quantityChange), movementType, note, createdBy);
        }
    }

    private String getMovementTypeText(String movementType) {
        switch (movementType) {
            case "IN": return "Nhập kho";
            case "OUT": return "Xuất kho";
            case "SALE": return "Bán hàng";
            case "RETURN": return "Hoàn trả";
            case "DAMAGE": return "Hàng hỏng";
            case "MANUAL_IN": return "Cập nhật tăng";
            case "MANUAL_OUT": return "Cập nhật giảm";
            default: return movementType;
        }
    }
}