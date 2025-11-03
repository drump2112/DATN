package com.example.DATN.controllers;

import com.example.DATN.models.Order;
import com.example.DATN.services.OrderService;
import com.example.DATN.configs.vnpay.VNPayService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
public class VNPayController {

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private OrderService orderService;

    // IPN Endpoint - VNPay gọi để confirm thanh toán (QUAN TRỌNG!)
    @GetMapping("/vnpay/ipn")
    public String vnpayIPN(HttpServletRequest request) {
        try {
            Map<String, String> fields = vnPayService.getFieldsFromRequest(request);

            System.out.println("=== VNPay IPN Received ===");
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }

            boolean isValidSignature = vnPayService.validateSignature(fields);
            String responseCode = fields.get("vnp_ResponseCode");
            String txnRef = fields.get("vnp_TxnRef");
            String amount = fields.get("vnp_Amount");
            String transactionNo = fields.get("vnp_TransactionNo");

            if (isValidSignature && "00".equals(responseCode)) {
                // Thanh toán thành công - XỬ LÝ THẬT ở đây
                try {
                    Integer orderId = Integer.parseInt(txnRef);
                    Order order = orderService.findById(orderId);

                    if (order != null && !"COMPLETED".equals(order.getStatus())) {
                        // CHỈ xử lý nếu order chưa được xử lý (tránh duplicate)
                        orderService.updatePaymentStatus(order, "COMPLETED", transactionNo);

                        // TRỪ KHO SẢN PHẨM ở đây
                        orderService.processOrderItems(order);

                        System.out.println("✅ VNPay IPN: Order " + orderId + " processed successfully");
                        return "RspCode=00&Message=Confirm Success"; // Trả về cho VNPay
                    } else {
                        System.out.println("⚠️ VNPay IPN: Order " + orderId + " already processed or not found");
                        return "RspCode=00&Message=Order Already Processed";
                    }
                } catch (Exception e) {
                    System.err.println("❌ VNPay IPN Error: " + e.getMessage());
                    return "RspCode=99&Message=Unknown error";
                }
            } else {
                System.err.println("❌ VNPay IPN: Invalid signature or failed payment");
                return "RspCode=97&Message=Invalid Signature";
            }
        } catch (Exception e) {
            System.err.println("❌ VNPay IPN Exception: " + e.getMessage());
            return "RspCode=99&Message=Unknown error";
        }
    }

    @GetMapping("/vnpay/return")
    public String vnpayReturn(HttpServletRequest request, Model model) {
        try {
            Map<String, String> fields = vnPayService.getFieldsFromRequest(request);

            // Validate the response
            boolean isValidSignature = vnPayService.validateSignature(fields);
            String responseCode = fields.get("vnp_ResponseCode");
            String txnRef = fields.get("vnp_TxnRef"); // This is our order ID
            String amount = fields.get("vnp_Amount");
            String transactionNo = fields.get("vnp_TransactionNo");

            model.addAttribute("isValidSignature", isValidSignature);
            model.addAttribute("responseCode", responseCode);
            model.addAttribute("txnRef", txnRef);
            model.addAttribute("amount", amount);
            model.addAttribute("transactionNo", transactionNo);

            if (isValidSignature && "00".equals(responseCode)) {
                // Chỉ hiển thị kết quả - KHÔNG xử lý thanh toán (đã xử lý ở IPN)
                try {
                    Integer orderId = Integer.parseInt(txnRef);
                    Order order = orderService.findById(orderId);
                    if (order != null) {
                        model.addAttribute("paymentStatus", "SUCCESS");
                        model.addAttribute("order", order);
                        System.out.println("✅ VNPay Return: Showing success page for order " + orderId);
                    } else {
                        model.addAttribute("paymentStatus", "ORDER_NOT_FOUND");
                    }
                } catch (Exception e) {
                    model.addAttribute("paymentStatus", "ERROR");
                    model.addAttribute("errorMessage", e.getMessage());
                }
            } else {
                // Payment failed
                model.addAttribute("paymentStatus", "FAILED");
                model.addAttribute("failureReason", getFailureReason(responseCode));
            }

        } catch (Exception e) {
            model.addAttribute("paymentStatus", "ERROR");
            model.addAttribute("errorMessage", e.getMessage());
        }

        return "shop/payment-result";
    }

    private String getFailureReason(String responseCode) {
        switch (responseCode) {
            case "01": return "Giao dịch chưa hoàn tất";
            case "02": return "Giao dịch bị lỗi";
            case "04": return "Giao dịch đảo (Khách hàng đã bị trừ tiền tại Ngân hàng nhưng GD chưa thành công ở VNPAY)";
            case "05": return "VNPAY đang xử lý giao dịch này (GD hoàn tiền)";
            case "06": return "VNPAY đã gửi yêu cầu hoàn tiền sang Ngân hàng (GD hoàn tiền)";
            case "07": return "Giao dịch bị nghi ngờ gian lận";
            case "09": return "GD Hoàn trả bị từ chối";
            case "10": return "Đã giao hàng";
            case "11": return "Giao dịch không thành công do: Khách hàng nhập sai mật khẩu OTP quá số lần quy định. Hoặc Tài khoản bị khóa.";
            case "12": return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa.";
            case "13": return "Giao dịch không thành công do Quý khách nhập sai mật khẩu xác thực giao dịch (OTP).";
            case "24": return "Giao dịch không thành công do: Khách hàng hủy giao dịch";
            case "51": return "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch.";
            case "65": return "Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày.";
            case "75": return "Ngân hàng thanh toán đang bảo trì.";
            case "79": return "Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định. Xin quý khách vui lòng thực hiện lại giao dịch";
            case "99": return "Các lỗi khác (lỗi còn lại, không có trong danh sách mã lỗi đã liệt kê)";
            default: return "Lỗi không xác định";
        }
    }
}