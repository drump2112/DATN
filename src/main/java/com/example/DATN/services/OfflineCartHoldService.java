package com.example.DATN.services;

import java.util.Map;

/**
 * Service để quản lý việc giữ tạm số lượng sản phẩm khi bán hàng offline (tại quầy)
 * Khi thêm sản phẩm vào giỏ hàng offline, số lượng sẽ được trừ ngay trong database
 * Khi hủy hoặc reload trang, số lượng sẽ được hoàn lại
 */
public interface OfflineCartHoldService {

    /**
     * Trừ số lượng sản phẩm trong database khi thêm vào giỏ hàng offline
     * @param variantId ID của variant
     * @param quantity Số lượng cần trừ
     * @param sessionId Session ID để theo dõi
     * @return true nếu thành công
     */
    boolean holdStock(Integer variantId, int quantity, String sessionId);

    /**
     * Hoàn lại số lượng sản phẩm khi xóa khỏi giỏ hàng
     * @param variantId ID của variant
     * @param quantity Số lượng cần hoàn lại
     * @param sessionId Session ID
     * @return true nếu thành công
     */
    boolean releaseStock(Integer variantId, int quantity, String sessionId);

    /**
     * Hoàn lại tất cả số lượng đang giữ của một session (khi reload hoặc đóng tab)
     * @param sessionId Session ID
     * @param heldItems Map chứa variantId và quantity đang giữ
     * @return true nếu thành công
     */
    boolean releaseAllStock(String sessionId, Map<Integer, Integer> heldItems);

    /**
     * Cập nhật số lượng giữ (khi thay đổi số lượng trong giỏ)
     * @param variantId ID của variant
     * @param oldQuantity Số lượng cũ
     * @param newQuantity Số lượng mới
     * @param sessionId Session ID
     * @return true nếu thành công
     */
    boolean updateHoldStock(Integer variantId, int oldQuantity, int newQuantity, String sessionId);

    /**
     * Lấy số lượng tồn kho thực tế của variant
     * @param variantId ID của variant
     * @return số lượng tồn kho
     */
    int getAvailableStock(Integer variantId);
}
