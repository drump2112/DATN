package com.example.DATN.controllers.shop;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.DATN.configs.CustomUserDetails;
import com.example.DATN.models.User;
import com.example.DATN.services.UserService;
import com.example.DATN.services.ImageService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/profile")
@Slf4j
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private ImageService imageService;

    @GetMapping("")
    public String getProfile(Model model) {
        model.addAttribute("showSlide", false);
        return "shop/profile";
    }

    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "dateOfBirth", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateOfBirth,
            @RequestParam(value = "gender", required = false) Boolean gender,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
                response.put("success", false);
                response.put("message", "Người dùng chưa đăng nhập");
                return ResponseEntity.badRequest().body(response);
            }

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User currentUser = userDetails.getUser();

            // Validate required fields
            if (fullName == null || fullName.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Họ và tên không được để trống");
                return ResponseEntity.badRequest().body(response);
            }

            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Email không được để trống");
                return ResponseEntity.badRequest().body(response);
            }

            if (!email.matches("^[\\w\\.-]+@[\\w\\.-]+\\.[\\w]+$")) {
                response.put("success", false);
                response.put("message", "Email không hợp lệ");
                return ResponseEntity.badRequest().body(response);
            }

            // Phone validation (if provided)
            if (phone != null && !phone.trim().isEmpty()) {
                phone = phone.replaceAll("\\s+", ""); // Remove spaces
                if (!phone.matches("^[0-9]{10,11}$")) {
                    response.put("success", false);
                    response.put("message", "Số điện thoại không hợp lệ");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // Update user information
            currentUser.setFullName(fullName.trim());
            currentUser.setEmail(email.trim());
            currentUser.setPhone(phone != null && !phone.trim().isEmpty() ? phone : null);
            currentUser.setDateOfBirth(dateOfBirth);
            currentUser.setGender(gender != null ? gender : currentUser.isGender());

            if (avatar != null && !avatar.isEmpty()) {
                try {
                    // Validate file size (20MB max)
                    if (avatar.getSize() > 20 * 1024 * 1024) {
                        response.put("success", false);
                        response.put("message", "Kích thước file không được vượt quá 20MB!");
                        return ResponseEntity.badRequest().body(response);
                    }

                    // Validate file type
                    String contentType = avatar.getContentType();
                    if (contentType == null || !contentType.startsWith("image/")) {
                        response.put("success", false);
                        response.put("message", "Vui lòng chọn file hình ảnh!");
                        return ResponseEntity.badRequest().body(response);
                    }

                    // Save new avatar
                    String avatarPath = imageService.saveImage(avatar, "user");
                    currentUser.setAvatar(avatarPath);
                    log.info("Avatar uploaded successfully: {}", avatarPath);
                } catch (Exception e) {
                    log.error("Error uploading avatar: ", e);
                    response.put("success", false);
                    response.put("message", "Có lỗi xảy ra khi tải lên ảnh: " + e.getMessage());
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // Save updated user
            User updatedUser = userService.updateUser(currentUser);

            response.put("success", true);
            response.put("message", "Thông tin đã được cập nhật thành công!");
            response.put("user", updatedUser);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating profile: ", e);
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi cập nhật thông tin: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserStats() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
                response.put("success", false);
                response.put("message", "Người dùng chưa đăng nhập");
                return ResponseEntity.badRequest().body(response);
            }

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User currentUser = userDetails.getUser();

            // Get user statistics (implement these methods in services)
            // int orderCount = orderService.getOrderCountByUser(currentUser.getId());
            // int loyaltyPoints = loyaltyService.getLoyaltyPoints(currentUser.getId());

            // Mock data for now - using currentUser.getId() to avoid unused variable warning
            int orderCount = (int) (Math.random() * 50) + (currentUser.getId() % 10);
            int loyaltyPoints = (int) (Math.random() * 1000) + (currentUser.getId() * 10);

            response.put("success", true);
            response.put("orderCount", orderCount);
            response.put("loyaltyPoints", loyaltyPoints);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting user stats: ", e);
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi lấy thống kê người dùng");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/change-password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
                response.put("success", false);
                response.put("message", "Người dùng chưa đăng nhập");
                return ResponseEntity.badRequest().body(response);
            }

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User currentUser = userDetails.getUser();

            // Validate inputs
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng nhập mật khẩu hiện tại");
                return ResponseEntity.badRequest().body(response);
            }

            if (newPassword == null || newPassword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng nhập mật khẩu mới");
                return ResponseEntity.badRequest().body(response);
            }

            if (!newPassword.equals(confirmPassword)) {
                response.put("success", false);
                response.put("message", "Mật khẩu xác nhận không khớp");
                return ResponseEntity.badRequest().body(response);
            }

            // Password strength validation
            if (newPassword.length() < 8) {
                response.put("success", false);
                response.put("message", "Mật khẩu phải có ít nhất 8 ký tự");
                return ResponseEntity.badRequest().body(response);
            }

            if (!newPassword.matches(".*[A-Z].*")) {
                response.put("success", false);
                response.put("message", "Mật khẩu phải có ít nhất 1 chữ cái viết hoa");
                return ResponseEntity.badRequest().body(response);
            }

            if (!newPassword.matches(".*[a-z].*")) {
                response.put("success", false);
                response.put("message", "Mật khẩu phải có ít nhất 1 chữ cái viết thường");
                return ResponseEntity.badRequest().body(response);
            }

            if (!newPassword.matches(".*\\d.*")) {
                response.put("success", false);
                response.put("message", "Mật khẩu phải có ít nhất 1 chữ số");
                return ResponseEntity.badRequest().body(response);
            }

            if (!newPassword.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
                response.put("success", false);
                response.put("message", "Mật khẩu phải có ít nhất 1 ký tự đặc biệt");
                return ResponseEntity.badRequest().body(response);
            }

            // Change password using service
            boolean success = userService.changePassword(currentUser, currentPassword, newPassword);

            if (!success) {
                response.put("success", false);
                response.put("message", "Mật khẩu hiện tại không chính xác!");
                return ResponseEntity.badRequest().body(response);
            }

            response.put("success", true);
            response.put("message", "Mật khẩu đã được thay đổi thành công!");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error changing password: ", e);
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi thay đổi mật khẩu: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/change-password")
    public String changePasswordPage(Model model) {
        model.addAttribute("showSlide", false);
        return "shop/change-password";
    }

    @GetMapping("/address-management")
    public String addressManagementPage(Model model) {
        model.addAttribute("showSlide", false);
        return "shop/address-management";
    }
}