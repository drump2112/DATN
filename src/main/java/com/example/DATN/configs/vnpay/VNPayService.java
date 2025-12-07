package com.example.DATN.configs.vnpay;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.DATN.models.Order;

@Service
public class VNPayService {

    @Value("${vnp_TmnCode}")
    private String vnp_TmnCode;

    @Value("${vnp_HashSecret}")
    private String vnp_HashSecret;

    @Value("${vnp_Url}")
    private String vnp_Url;

    @Value("${vnp_ReturnUrl}")
    private String vnp_ReturnUrl;

    @Value("${vnp_IpnUrl}")
    private String vnp_IpnUrl;

    public String createPaymentUrl(Order order, HttpServletRequest request) throws UnsupportedEncodingException {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        if (order.getTotalAmount() == null || order.getTotalAmount().longValue() <= 0) {
            throw new IllegalArgumentException("Invalid order amount: " + order.getTotalAmount());
        }

        if (vnp_TmnCode == null || vnp_TmnCode.trim().isEmpty()) {
            throw new IllegalStateException("VNPay Terminal Code (vnp_TmnCode) is not configured");
        }
        if (vnp_HashSecret == null || vnp_HashSecret.trim().isEmpty()) {
            throw new IllegalStateException("VNPay Hash Secret (vnp_HashSecret) is not configured");
        }
        if (vnp_Url == null || vnp_Url.trim().isEmpty()) {
            throw new IllegalStateException("VNPay URL (vnp_Url) is not configured");
        }

        System.out.println("=== Creating VNPay payment URL ===");
        System.out.println("Order ID: " + order.getId());
        System.out.println("Order Code: " + order.getOrderCode());
        System.out.println("Total Amount: " + order.getTotalAmount());
        System.out.println("VNP_TmnCode: " + vnp_TmnCode);
        System.out.println("VNP_Url: " + vnp_Url);

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        String vnp_CreateDate = formatter.format(new Date());

        Date expireTime = new Date(System.currentTimeMillis() + 15 * 60 * 1000);
        String vnp_ExpireDate = formatter.format(expireTime);
        // String vnp_CreateDate = "20241103171039";
        // String vnp_ExpireDate = "20241103172539";
        String clientIp = getIpAddress(request);

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", vnp_Version);
        params.put("vnp_Command", vnp_Command);
        params.put("vnp_TmnCode", vnp_TmnCode);
        long amount = order.getTotalAmount().multiply(new BigDecimal(100)).longValue();

        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_CurrCode", "VND");
        String txnRef = order.getId() + "_" + System.currentTimeMillis();
        params.put("vnp_TxnRef", txnRef);

        String orderInfo = "Payment for order " + order.getOrderCode();
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        // params.put("vnp_IpnUrl", vnp_IpnUrl);
        params.put("vnp_CreateDate", vnp_CreateDate);
        params.put("vnp_ExpireDate", vnp_ExpireDate);
        params.put("vnp_IpAddr", clientIp);

        System.out.println("=== VNPay Parameters Validation ===");

        if (amount < 500000) {
            throw new IllegalArgumentException(
                    "VNPay amount must be >= 5,000 VND, current: " + (amount / 100) + " VND");
        }
        if (amount > 10000000000L) {
            throw new IllegalArgumentException("VNPay amount exceeds sandbox limit: " + (amount / 100) + " VND");
        }

        if (txnRef.length() > 100) {
            throw new IllegalArgumentException("TxnRef too long: " + txnRef.length() + " chars");
        }
        if (orderInfo.length() > 255) {
            throw new IllegalArgumentException("OrderInfo too long: " + orderInfo.length() + " chars");
        }

        if (!isValidIP(clientIp)) {
            System.err.println("Warning: Invalid IP format: " + clientIp + ", using fallback");
            clientIp = "127.0.0.1";
            params.put("vnp_IpAddr", clientIp);
        }

        if (!vnp_ReturnUrl.startsWith("http")) {
            throw new IllegalArgumentException("Invalid return URL format: " + vnp_ReturnUrl);
        }
        if (!vnp_IpnUrl.startsWith("http")) {
            throw new IllegalArgumentException("Invalid IPN URL format: " + vnp_IpnUrl);
        }

        System.out.println("Amount VND: " + order.getTotalAmount());
        System.out.println("Amount send to VNPay: " + amount);
        System.out.println("TxnRef: " + txnRef + " (length: " + txnRef.length() + ")");
        System.out.println("OrderInfo: " + orderInfo + " (length: " + orderInfo.length() + ")");
        System.out.println("Client IP: " + clientIp);
        System.out.println("Return URL: " + vnp_ReturnUrl);
        System.out.println("IPN URL: " + vnp_IpnUrl);
        System.out.println("Create Date: " + vnp_CreateDate);
        System.out.println("Expire Date: " + vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames); // Sắp xếp theo alphabet - quan trọng cho VNPay
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        System.out.println("=== PARAMETER SORTING CHECK ===");
        System.out.println("Sorted parameter order: " + String.join(", ", fieldNames));

        String[] expectedOrder = {
                "vnp_Amount", "vnp_Command", "vnp_CreateDate", "vnp_CurrCode",
                "vnp_ExpireDate", "vnp_IpAddr", "vnp_IpnUrl", "vnp_Locale",
                "vnp_OrderInfo", "vnp_OrderType", "vnp_ReturnUrl", "vnp_TmnCode",
                "vnp_TxnRef", "vnp_Version"
        };
        System.out.println("Expected VNPay order: " + String.join(", ", expectedOrder));

        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                String encodedValue = java.net.URLEncoder.encode(fieldValue, "UTF-8");

                hashData.append(fieldName).append('=').append(encodedValue);

                query.append(java.net.URLEncoder.encode(fieldName, "UTF-8"))
                        .append('=')
                        .append(encodedValue);

                if (i < fieldNames.size() - 1) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);

        String finalUrl = vnp_Url + "?" + query.toString();

        System.out.println("=== VNPay URL Created Successfully ===");
        System.out.println("Final URL length: " + finalUrl.length());
        System.out.println("=== HASH VS QUERY COMPARISON ===");
        System.out.println("Hash data (raw): " + hashData.toString());
        System.out.println("Query string (encoded): " + query.toString());
        System.out.println("Secure hash: " + vnp_SecureHash);

        System.out.println("=== All VNPay Parameters (Sorted) ===");
        for (String key : fieldNames) {
            System.out.println(key + "=" + params.get(key));
        }

        System.out.println("=== VNPAY COMPATIBILITY CHECK ===");
        System.out.println("- vnp_Version: " + params.get("vnp_Version") + " (should be 2.1.0)");
        System.out.println("- vnp_Command: " + params.get("vnp_Command") + " (should be pay)");
        System.out.println("- vnp_TmnCode: " + params.get("vnp_TmnCode") + " (should be FJDRZR2R)");
        System.out.println("- vnp_Amount: " + params.get("vnp_Amount") + " (in VND cents)");
        System.out.println("- vnp_CurrCode: " + params.get("vnp_CurrCode") + " (should be VND)");
        System.out.println("- vnp_TxnRef length: " + params.get("vnp_TxnRef").length() + " chars");
        System.out.println("- vnp_OrderInfo: " + params.get("vnp_OrderInfo"));
        System.out.println("- vnp_OrderType: " + params.get("vnp_OrderType") + " (should be other)");
        System.out.println("- vnp_Locale: " + params.get("vnp_Locale") + " (should be vn)");
        System.out.println("- vnp_CreateDate format: " + params.get("vnp_CreateDate") + " (yyyyMMddHHmmss)");
        System.out.println("- vnp_ExpireDate format: " + params.get("vnp_ExpireDate") + " (yyyyMMddHHmmss)");
        System.out.println("- vnp_IpAddr: " + params.get("vnp_IpAddr"));
        System.out.println("- Parameter count: " + fieldNames.size() + " fields");

        validateVNPayParameters(params);

        if (finalUrl.length() > 2048) {
            System.err.println("WARNING: VNPay URL is too long (" + finalUrl.length() + " chars)");
        }

        System.out.println("=== FINAL VALIDATION FOR ERROR 99 ===");
        System.out.println("- Amount validation: " + (amount >= 500000 ? "PASS" : "FAIL - Too low"));
        System.out.println("- TxnRef validation: " + (txnRef.length() <= 100 ? "PASS" : "FAIL - Too long"));
        System.out.println("- OrderInfo validation: " + (orderInfo.length() <= 255 ? "PASS" : "FAIL - Too long"));
        System.out.println("- IP validation: " + (isValidIP(clientIp) ? "PASS" : "FAIL - Invalid IP"));
        System.out.println("- Encoding: UTF-8 for both URL and hash");
        System.out.println("- Hash length: " + vnp_SecureHash.length() + " chars (should be 128)");
        System.out.println("- TmnCode: " + vnp_TmnCode + " (should be FJDRZR2R)");
        System.out.println("- OrderInfo: " + orderInfo);
        System.out.println("- Return URL in hash: " + vnp_ReturnUrl);
        System.out.println("- IPN URL in hash: " + vnp_IpnUrl);
        try {
            System.out.println("✓ Return URL encoded: " + java.net.URLEncoder.encode(vnp_ReturnUrl, "UTF-8"));
            System.out.println("✓ IPN URL encoded: " + java.net.URLEncoder.encode(vnp_IpnUrl, "UTF-8"));
        } catch (Exception e) {
            System.err.println("Failed to encode URLs for display");
        }

        return finalUrl;
    }

    public Map<String, String> getFieldsFromRequest(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String fieldName = parameterNames.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                fields.put(fieldName, fieldValue);
            }
        }

        return fields;
    }

    // public boolean validateSignature(Map<String, String> fields) {
    // String vnp_SecureHash = fields.get("vnp_SecureHash");
    // fields.remove("vnp_SecureHashType");
    // fields.remove("vnp_SecureHash");

    // List<String> fieldNames = new ArrayList<>(fields.keySet());
    // Collections.sort(fieldNames);
    // StringBuilder hashData = new StringBuilder();

    // for (int i = 0; i < fieldNames.size(); i++) {
    // String fieldName = fieldNames.get(i);
    // String fieldValue = fields.get(fieldName);
    // if (fieldValue != null && fieldValue.length() > 0) {
    // hashData.append(fieldName).append('=').append(fieldValue);
    // if (i < fieldNames.size() - 1) {
    // hashData.append('&');
    // }
    // }
    // }

    // String signValue = hmacSHA512(vnp_HashSecret, hashData.toString());
    // return signValue.equals(vnp_SecureHash);
    // }

    public boolean validateSignature(Map<String, String> fields) throws UnsupportedEncodingException {
        String vnp_SecureHash = fields.get("vnp_SecureHash");

        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                // IMPORTANT: phải URLEncode giống lúc tạo URL
                String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString());
                // VNPay trước kia dùng URLEncoder -> giữ nguyên kết quả đó
                hashData.append(fieldName).append('=').append(encodedValue);
                if (i < fieldNames.size() - 1) {
                    hashData.append('&');
                }
            }
        }

        // Trim secret key (loại space thừa nếu có)
        String secret = (vnp_HashSecret == null) ? "" : vnp_HashSecret.trim();

        String signValue = hmacSHA512(secret, hashData.toString());

        System.out.println("== VNPAY validateSignature debug ==");
        System.out.println("Provided vnp_SecureHash: " + vnp_SecureHash);
        System.out.println("Computed signValue     : " + signValue);
        System.out.println("HashData used          : " + hashData.toString());

        // So sánh ignoring case cho an toàn
        return signValue != null && vnp_SecureHash != null && signValue.equalsIgnoreCase(vnp_SecureHash);
    }

    private boolean isValidIP(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }

        try {
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        if ("0:0:0:0:0:0:0:1".equals(ipAddress) || "::1".equals(ipAddress)) {
            ipAddress = "127.0.0.1";
        }

        System.out.println("Client IP Address: " + ipAddress);

        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = "127.0.0.1";
        }

        return ipAddress;
    }

    // private String hmacSHA512(String key, String data) {
    // try {
    // System.out.println("=== HMAC Calculation Debug ===");
    // System.out.println("Key length: " + key.length());
    // System.out.println("Data to hash: " + data);

    // Mac hmac512 = Mac.getInstance("HmacSHA512");
    // SecretKeySpec secretKeySpec = new
    // SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
    // hmac512.init(secretKeySpec);
    // byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
    // StringBuilder hash = new StringBuilder();
    // for (byte b : bytes) {
    // hash.append(String.format("%02x", b));
    // }

    // System.out.println("Generated hash: " + hash.toString());
    // System.out.println("Hash length: " + hash.toString().length());

    // return hash.toString();
    // } catch (Exception e) {
    // System.err.println("HMAC calculation error: " + e.getMessage());
    // e.printStackTrace();
    // return "";
    // }
    // }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKeySpec);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }
            return hash.toString();
        } catch (Exception e) {
            System.err.println("HMAC calculation error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void validateVNPayParameters(Map<String, String> params) {
        System.out.println("=== VNPAY PARAMETER VALIDATION ===");

        String[] requiredParams = {
                "vnp_Version", "vnp_Command", "vnp_TmnCode", "vnp_Amount", "vnp_CurrCode",
                "vnp_TxnRef", "vnp_OrderInfo", "vnp_ReturnUrl", "vnp_CreateDate", "vnp_IpAddr"
        };

        for (String param : requiredParams) {
            if (!params.containsKey(param) || params.get(param) == null || params.get(param).isEmpty()) {
                System.err.println("MISSING REQUIRED PARAMETER: " + param);
            } else {
                System.out.println(param + ": " + params.get(param));
            }
        }

        // Check parameter values
        if (!"2.1.0".equals(params.get("vnp_Version"))) {
            System.err.println(" Wrong vnp_Version: " + params.get("vnp_Version"));
        }
        if (!"pay".equals(params.get("vnp_Command"))) {
            System.err.println("Wrong vnp_Command: " + params.get("vnp_Command"));
        }
        if (!"VND".equals(params.get("vnp_CurrCode"))) {
            System.err.println("Wrong vnp_CurrCode: " + params.get("vnp_CurrCode"));
        }
        if (!"FJDRZR2R".equals(params.get("vnp_TmnCode"))) {
            System.err.println("Wrong vnp_TmnCode: " + params.get("vnp_TmnCode"));
        }

        // Check amount format (must be integer, >= 5000 VND in cents)
        try {
            long amount = Long.parseLong(params.get("vnp_Amount"));
            if (amount < 500000) {
                System.err.println("Amount too low: " + amount + " (should be >= 500000)");
            }
        } catch (Exception e) {
            System.err.println("Invalid amount format: " + params.get("vnp_Amount"));
        }

        // Check date format
        String createDate = params.get("vnp_CreateDate");
        if (createDate == null || createDate.length() != 14) {
            System.err.println("Invalid vnp_CreateDate format: " + createDate);
        }

        String expireDate = params.get("vnp_ExpireDate");
        if (expireDate == null || expireDate.length() != 14) {
            System.err.println("Invalid vnp_ExpireDate format: " + expireDate);
        }

        System.out.println("=== VALIDATION COMPLETE ===");
    }

    public void testHashCalculation() {
        String testData = "vnp_Amount=10000000&vnp_Command=pay&vnp_CreateDate=20210801153333&vnp_CurrCode=VND&vnp_IpAddr=127.0.0.1&vnp_Locale=vn&vnp_OrderInfo=OrderTest&vnp_OrderType=other&vnp_ReturnUrl=http://localhost:8080/vnpay/return&vnp_TmnCode=DEMOV210&vnp_TxnRef=1234567890&vnp_Version=2.1.0";
        String expectedHash = hmacSHA512(vnp_HashSecret, testData);
        System.out.println("Test hash calculation: " + expectedHash);
    }

}
