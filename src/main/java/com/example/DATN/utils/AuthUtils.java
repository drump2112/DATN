package com.example.DATN.utils;

import com.example.DATN.configs.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

public class AuthUtils {
    /**
     * Lấy tên người dùng hiện tại từ authentication context
     * Nếu không đăng nhập, trả về "system"
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }

    /**
     * Lấy tên đầy đủ người dùng hiện tại từ authentication context
     * Nếu không đăng nhập, trả về "system"
     */
    public static String getCurrentFullName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                String fullName = userDetails.getUser().getFullName();
                if (fullName != null && !fullName.isEmpty()) {
                    return fullName;
                }
                return userDetails.getUsername();
            }
            return authentication.getName();
        }
        return "system";
    }
}
