package com.example.DATN.services.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.DATN.models.ProductVariant;
import com.example.DATN.repositories.ProductVariantRepository;
import com.example.DATN.services.OfflineCartHoldService;

/**
 * Service để quản lý việc giữ tạm số lượng sản phẩm khi bán hàng offline (tại quầy)
 * Trừ trực tiếp trong database khi thêm vào giỏ hàng
 * Hoàn lại khi xóa khỏi giỏ hoặc reload trang
 */
@Service
public class OfflineCartHoldServiceImpl implements OfflineCartHoldService {

    private static final Logger logger = LoggerFactory.getLogger(OfflineCartHoldServiceImpl.class);

    @Autowired
    private ProductVariantRepository productVariantRepository;

    // Theo dõi số lượng đang giữ của mỗi session để có thể hoàn lại khi cần
    // Key: sessionId, Value: Map<variantId, quantity>
    private final ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> sessionHolds = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public boolean holdStock(Integer variantId, int quantity, String sessionId) {
        try {
            // Lấy variant với lock để tránh race condition
            ProductVariant variant = productVariantRepository.findByIdForUpdate(variantId);
            if (variant == null) {
                logger.error("Không tìm thấy ProductVariant với id: {}", variantId);
                return false;
            }

            int currentStock = variant.getQuantity();
            if (currentStock < quantity) {
                logger.warn("Không đủ tồn kho. VariantId: {}, Yêu cầu: {}, Còn: {}", variantId, quantity, currentStock);
                return false;
            }

            // Trừ số lượng trong database
            variant.setQuantity(currentStock - quantity);
            productVariantRepository.save(variant);

            // Lưu lại thông tin để có thể hoàn lại sau này
            sessionHolds.computeIfAbsent(sessionId, k -> new ConcurrentHashMap<>())
                    .merge(variantId, quantity, Integer::sum);

            logger.info("Đã giữ {} sản phẩm của variantId {} cho session {}. Tồn kho còn: {}",
                    quantity, variantId, sessionId, currentStock - quantity);
            return true;

        } catch (Exception e) {
            logger.error("Lỗi khi giữ tồn kho: ", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean releaseStock(Integer variantId, int quantity, String sessionId) {
        try {
            ProductVariant variant = productVariantRepository.findByIdForUpdate(variantId);
            if (variant == null) {
                logger.error("Không tìm thấy ProductVariant với id: {}", variantId);
                return false;
            }

            // Hoàn lại số lượng vào database
            int currentStock = variant.getQuantity();
            variant.setQuantity(currentStock + quantity);
            productVariantRepository.save(variant);

            // Cập nhật thông tin session hold
            ConcurrentHashMap<Integer, Integer> sessionHold = sessionHolds.get(sessionId);
            if (sessionHold != null) {
                sessionHold.computeIfPresent(variantId, (k, v) -> {
                    int newValue = v - quantity;
                    return newValue <= 0 ? null : newValue; // Xóa nếu về 0
                });
                // Xóa session nếu không còn item nào
                if (sessionHold.isEmpty()) {
                    sessionHolds.remove(sessionId);
                }
            }

            logger.info("Đã hoàn lại {} sản phẩm của variantId {} cho session {}. Tồn kho: {}",
                    quantity, variantId, sessionId, currentStock + quantity);
            return true;

        } catch (Exception e) {
            logger.error("Lỗi khi hoàn lại tồn kho: ", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean releaseAllStock(String sessionId, Map<Integer, Integer> heldItems) {
        try {
            if (heldItems == null || heldItems.isEmpty()) {
                logger.info("Không có sản phẩm nào cần hoàn lại cho session {}", sessionId);
                return true;
            }

            for (Map.Entry<Integer, Integer> entry : heldItems.entrySet()) {
                Integer variantId = entry.getKey();
                Integer quantity = entry.getValue();

                ProductVariant variant = productVariantRepository.findByIdForUpdate(variantId);
                if (variant != null) {
                    int currentStock = variant.getQuantity();
                    variant.setQuantity(currentStock + quantity);
                    productVariantRepository.save(variant);
                    logger.info("Đã hoàn lại {} sản phẩm của variantId {}. Tồn kho: {}",
                            quantity, variantId, currentStock + quantity);
                }
            }

            // Xóa thông tin session
            sessionHolds.remove(sessionId);

            logger.info("Đã hoàn lại tất cả {} sản phẩm cho session {}",
                    heldItems.size(), sessionId);
            return true;

        } catch (Exception e) {
            logger.error("Lỗi khi hoàn lại tất cả tồn kho: ", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateHoldStock(Integer variantId, int oldQuantity, int newQuantity, String sessionId) {
        try {
            int difference = newQuantity - oldQuantity;

            if (difference == 0) {
                return true; // Không có thay đổi
            }

            ProductVariant variant = productVariantRepository.findByIdForUpdate(variantId);
            if (variant == null) {
                logger.error("Không tìm thấy ProductVariant với id: {}", variantId);
                return false;
            }

            int currentStock = variant.getQuantity();

            if (difference > 0) {
                // Cần trừ thêm số lượng
                if (currentStock < difference) {
                    logger.warn("Không đủ tồn kho để tăng. VariantId: {}, Cần thêm: {}, Còn: {}",
                            variantId, difference, currentStock);
                    return false;
                }
                variant.setQuantity(currentStock - difference);
            } else {
                // Hoàn lại số lượng (difference < 0, nên dùng -difference để cộng)
                variant.setQuantity(currentStock - difference); // currentStock + (-difference) = currentStock + |difference|
            }

            productVariantRepository.save(variant);

            // Cập nhật session hold
            sessionHolds.computeIfAbsent(sessionId, k -> new ConcurrentHashMap<>())
                    .merge(variantId, difference, Integer::sum);

            logger.info("Đã cập nhật số lượng giữ của variantId {} từ {} thành {} cho session {}. Tồn kho: {}",
                    variantId, oldQuantity, newQuantity, sessionId, variant.getQuantity());
            return true;

        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật số lượng giữ: ", e);
            return false;
        }
    }

    @Override
    public int getAvailableStock(Integer variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId).orElse(null);
        return variant != null ? variant.getQuantity() : 0;
    }
}
