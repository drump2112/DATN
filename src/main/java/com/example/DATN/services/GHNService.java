package com.example.DATN.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.DATN.configs.GHNMappingHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Service
public class GHNService {

    @Value("${ghn.api.url:https://dev-online-gateway.ghn.vn/shiip/public-api/v2}")
    private String ghnApiUrl;

    @Value("${ghn.api.token}")
    private String ghnToken;

    @Value("${ghn.shop.id}")
    private String ghnShopId;

    @Value("${ghn.from.district.id:1454}") // Quận Cầu Giấy, Hà Nội - mặc định
    private String fromDistrictId;

    @Value("${ghn.from.ward.code:21012}") // Phường Dịch Vọng - mặc định
    private String fromWardCode;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GHNMappingHelper mappingHelper;

    public GHNService(GHNMappingHelper mappingHelper) {
        this.mappingHelper = mappingHelper;
    }

    /**
     * Tính phí giao hàng dựa trên API Giao Hàng Nhanh
     * @param toProvinceCode Mã tỉnh đến
     * @param toCommuneCode Mã phường/xã đến
     * @param weight Trọng lượng (gram) - mặc định 500g
     * @param totalValue Giá trị đơn hàng (VNĐ) - để tính bảo hiểm
     * @return Phí giao hàng (VNĐ)
     */
    public double calculateShippingFee(String toProvinceCode, String toCommuneCode,
                                     Integer weight, Double totalValue) {
        try {
            // Bước 1: Lấy District ID từ Province Code
            String toDistrictId = getDistrictIdFromProvince(toProvinceCode);
            if (toDistrictId == null) {
                return getDefaultShippingFee();
            }

            // Bước 2: Lấy Ward Code từ Commune Code
            String toWardCode = getWardCodeFromCommune(toCommuneCode, toDistrictId);
            if (toWardCode == null) {
                return getDefaultShippingFee();
            }

            // Bước 3: Gọi API tính phí
            return calculateFeeFromGHN(toDistrictId, toWardCode, weight != null ? weight : 500,
                                     totalValue != null ? totalValue : 0);

        } catch (Exception e) {
            System.err.println("Error calculating shipping fee with GHN: " + e.getMessage());
            return getDefaultShippingFee();
        }
    }

    private String getDistrictIdFromProvince(String provinceCode) {
        try {
            String url = ghnApiUrl + "/master-data/district";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", ghnToken);

            // Body request với province_id (GHN sử dụng ID khác với mã tỉnh của ta)
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("province_id", convertProvinceCodeToGHNId(provinceCode));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                JsonNode data = jsonNode.get("data");

                if (data != null && data.isArray() && data.size() > 0) {
                    // Lấy district đầu tiên (thường là thành phố chính của tỉnh)
                    return data.get(0).get("DistrictID").asText();
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting district ID: " + e.getMessage());
        }
        return null;
    }

    private String getWardCodeFromCommune(String communeCode, String districtId) {
        try {
            String url = ghnApiUrl + "/master-data/ward";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", ghnToken);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("district_id", Integer.parseInt(districtId));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                JsonNode data = jsonNode.get("data");

                if (data != null && data.isArray() && data.size() > 0) {
                    // Tìm ward phù hợp hoặc lấy ward đầu tiên
                    for (JsonNode ward : data) {
                        // So sánh tên hoặc code nếu có
                        return ward.get("WardCode").asText();
                    }
                    // Fallback: lấy ward đầu tiên
                    return data.get(0).get("WardCode").asText();
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting ward code: " + e.getMessage());
        }
        return null;
    }

    private double calculateFeeFromGHN(String toDistrictId, String toWardCode,
                                      Integer weight, Double totalValue) {
        try {
            String url = ghnApiUrl + "/shipping-order/fee";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", ghnToken);
            headers.set("ShopId", ghnShopId);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("service_type_id", 2); // Standard service
            requestBody.put("from_district_id", Integer.parseInt(fromDistrictId));
            requestBody.put("from_ward_code", fromWardCode);
            requestBody.put("to_district_id", Integer.parseInt(toDistrictId));
            requestBody.put("to_ward_code", toWardCode);
            requestBody.put("weight", weight);
            requestBody.put("length", 20); // cm
            requestBody.put("width", 20);  // cm
            requestBody.put("height", 10); // cm
            requestBody.put("insurance_value", totalValue.intValue());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                JsonNode data = jsonNode.get("data");

                if (data != null) {
                    double totalFee = data.get("total").asDouble();
                    return totalFee;
                }
            }
        } catch (Exception e) {
            System.err.println("Error calculating fee from GHN API: " + e.getMessage());
        }

        return getDefaultShippingFee();
    }

    private Integer convertProvinceCodeToGHNId(String provinceCode) {
        return mappingHelper.getGHNProvinceId(provinceCode);
    }


    // Trả về phí giao dịch mặc định
    private double getDefaultShippingFee() {
        return 30000.0;
    }

    // Lấy danh sách dịch vụ vận chuyển khả dụng
    public String getAvailableServices(String toDistrictId) {
        try {
            String url = ghnApiUrl + "/shipping-order/available-services";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", ghnToken);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("shop_id", Integer.parseInt(ghnShopId));
            requestBody.put("from_district", Integer.parseInt(fromDistrictId));
            requestBody.put("to_district", Integer.parseInt(toDistrictId));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            return response.getBody();
        } catch (Exception e) {
            System.err.println("Error getting available services: " + e.getMessage());
            return null;
        }
    }
}