package com.example.DATN.services;

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
     */
    public double calculateShippingFee(String toProvinceCode, String toCommuneCode,
                                     Integer weight, Double totalValue) {
        try {
            System.out.println("=== GHNService.calculateShippingFee() ===");
            System.out.println("Province Code: " + toProvinceCode);
            System.out.println("Commune Code: " + toCommuneCode);
            System.out.println("Weight: " + weight + "g");
            System.out.println("Total Value: " + totalValue);

            // Bước 1: Convert Province Code sang GHN Province ID
            Integer ghnProvinceId = convertProvinceCodeToGHNId(toProvinceCode);
            System.out.println("GHN Province ID: " + ghnProvinceId);

            if (ghnProvinceId == null) {
                System.out.println("ERROR: Province ID is null, returning default 30000");
                return getDefaultShippingFee();
            }

            // Bước 2: Lấy District ID từ Province
            String toDistrictId = getDistrictIdFromProvince(ghnProvinceId);
            System.out.println("GHN District ID: " + toDistrictId);

            if (toDistrictId == null) {
                System.out.println("ERROR: District ID is null, returning default 30000");
                return getDefaultShippingFee();
            }

            // Bước 3: Lấy Ward Code từ District
            String toWardCode = getWardCodeFromDistrict(toDistrictId);
            System.out.println("GHN Ward Code: " + toWardCode);

            if (toWardCode == null) {
                System.out.println("ERROR: Ward Code is null, returning default 30000");
                return getDefaultShippingFee();
            }

            // Bước 4: Gọi API tính phí
            double fee = calculateFeeFromGHN(toDistrictId, toWardCode,
                                            weight != null ? weight : 500,
                                            totalValue != null ? totalValue : 0);
            System.out.println("GHN Fee Result: " + fee + " VND");
            return fee;

        } catch (Exception e) {
            System.err.println("ERROR calculating shipping fee: " + e.getMessage());
            e.printStackTrace();
            return getDefaultShippingFee();
        }
    }

    private String getDistrictIdFromProvince(Integer provinceId) {
        try {
            // QUAN TRỌNG: URL KHÔNG có /v2 cho master-data
            String url = ghnApiUrl.replace("/v2", "") + "/master-data/district";
            System.out.println("Calling: " + url + " with province_id=" + provinceId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", ghnToken);
            headers.set("ShopId", ghnShopId);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("province_id", provinceId);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            System.out.println("District API Response: " + response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                JsonNode data = jsonNode.get("data");

                if (data != null && data.isArray() && data.size() > 0) {
                    String districtId = data.get(0).get("DistrictID").asText();
                    String districtName = data.get(0).get("DistrictName").asText();
                    System.out.println("Got District: " + districtName + " (ID: " + districtId + ")");
                    return districtId;
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting district ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private String getWardCodeFromDistrict(String districtId) {
        try {
            // QUAN TRỌNG: URL KHÔNG có /v2 cho master-data
            String url = ghnApiUrl.replace("/v2", "") + "/master-data/ward";
            System.out.println("Calling: " + url + " with district_id=" + districtId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", ghnToken);
            headers.set("ShopId", ghnShopId);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("district_id", Integer.parseInt(districtId));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            System.out.println("Ward API Response: " + response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                JsonNode data = jsonNode.get("data");

                if (data != null && data.isArray() && data.size() > 0) {
                    String wardCode = data.get(0).get("WardCode").asText();
                    String wardName = data.get(0).get("WardName").asText();
                    System.out.println("Got Ward: " + wardName + " (Code: " + wardCode + ")");
                    return wardCode;
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting ward code: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private double calculateFeeFromGHN(String toDistrictId, String toWardCode,
                                      Integer weight, Double totalValue) {
        try {
            // QUAN TRỌNG: URL CÓ /v2 cho shipping-order
            String url = ghnApiUrl + "/shipping-order/fee";
            System.out.println("Calling: " + url);
            System.out.println("From: District=" + fromDistrictId + ", Ward=" + fromWardCode);
            System.out.println("To: District=" + toDistrictId + ", Ward=" + toWardCode);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", ghnToken);
            headers.set("ShopId", ghnShopId);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("service_type_id", 2);
            requestBody.put("from_district_id", Integer.parseInt(fromDistrictId));
            requestBody.put("from_ward_code", fromWardCode);
            requestBody.put("to_district_id", Integer.parseInt(toDistrictId));
            requestBody.put("to_ward_code", toWardCode);
            requestBody.put("weight", weight);
            requestBody.put("length", 20);
            requestBody.put("width", 20);
            requestBody.put("height", 10);
            requestBody.put("insurance_value", totalValue.intValue());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            System.out.println("Fee API Response: " + response.getStatusCode());
            System.out.println("Fee API Body: " + response.getBody());

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                JsonNode data = jsonNode.get("data");

                if (data != null) {
                    double totalFee = data.get("total").asDouble();
                    System.out.println("Calculated Fee: " + totalFee + " VND");
                    return totalFee;
                }
            }
        } catch (Exception e) {
            System.err.println("Error calculating fee from GHN API: " + e.getMessage());
            e.printStackTrace();
        }

        return getDefaultShippingFee();
    }

    private Integer convertProvinceCodeToGHNId(String provinceCode) {
        return mappingHelper.getGHNProvinceId(provinceCode);
    }

    private double getDefaultShippingFee() {
        return 30000.0;
    }

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