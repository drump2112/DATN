package com.example.DATN.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.DATN.configs.GHNMappingHelper;
import com.example.DATN.models.Commune;
import com.example.DATN.repositories.CommuneRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    private final CommuneRepository communeRepository;

    public GHNService(GHNMappingHelper mappingHelper, CommuneRepository communeRepository) {
        this.mappingHelper = mappingHelper;
        this.communeRepository = communeRepository;
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
            System.out.println("GHNService.calculateShippingFee() called with:");
            System.out.println("   - Province Code: " + toProvinceCode);
            System.out.println("   - Commune Code: " + toCommuneCode);
            System.out.println("   - Weight: " + weight + "g");
            System.out.println("   - Total Value: " + totalValue);

            // Lấy thông tin Commune từ database để lấy GHN Ward Code và District ID
            Optional<Commune> communeOpt = communeRepository.findById(toCommuneCode);

            if (!communeOpt.isPresent()) {
                System.out.println("Commune not found in database: " + toCommuneCode);
                return getDefaultShippingFee();
            }

            Commune commune = communeOpt.get();
            String toWardCode = commune.getGhnWardCode();
            Integer toDistrictId = commune.getGhnDistrictId();

            System.out.println("   - GHN Ward Code from DB: " + toWardCode);
            System.out.println("   - GHN District ID from DB: " + toDistrictId);

            // Nếu chưa có GHN Ward Code trong DB, fallback sang cách cũ
            if (toWardCode == null || toDistrictId == null) {
                System.out.println("GHN data not in DB, using fallback method...");
                return calculateShippingFeeFallback(toProvinceCode, toCommuneCode, weight, totalValue);
            }

            // Gọi API tính phí với dữ liệu từ DB
            double fee = calculateFeeFromGHN(toDistrictId.toString(), toWardCode,
                                             weight != null ? weight : 500,
                                             totalValue != null ? totalValue : 0);
            System.out.println("GHN returned fee: " + fee);
            return fee;

        } catch (Exception e) {
            System.err.println("Error calculating shipping fee with GHN: " + e.getMessage());
            e.printStackTrace();
            return getDefaultShippingFee();
        }
    }

    /**
     * Fallback method
     */
    private double calculateShippingFeeFallback(String toProvinceCode, String toCommuneCode,
                                                Integer weight, Double totalValue) {
        try {
            System.out.println("Using fallback method to get GHN data...");

            // Bước 1: Lấy Province ID từ Province Code
            Integer toProvinceId = convertProvinceCodeToGHNId(toProvinceCode);
            System.out.println("   - Province ID from Province Code: " + toProvinceId);
            if (toProvinceId == null) {
                System.out.println("Province ID is null, returning default 30000");
                return getDefaultShippingFee();
            }

            // Bước 2: Lấy Ward Code từ Commune Code (gọi trực tiếp với province_id, không cần district)
            String toWardCode = getWardCodeFromCommuneByProvince(toCommuneCode, toProvinceId);
            System.out.println("   - Ward Code from Commune: " + toWardCode);
            if (toWardCode == null) {
                System.out.println("Ward Code is null, returning default 30000");
                return getDefaultShippingFee();
            }

            // Bước 3: Lấy District ID từ Province (GHN fee API
            // cần district_id)
            String toDistrictId = getDistrictIdFromProvince(toProvinceCode);
            System.out.println("   - District ID from Province: " + toDistrictId);
            if (toDistrictId == null) {
                System.out.println("District ID is null but continuing with fee calculation");
            }

            // Bước 4: Gọi API tính phí
            double fee = calculateFeeFromGHN(toDistrictId != null ? toDistrictId : toProvinceId.toString(), toWardCode, weight != null ? weight : 500,
                                     totalValue != null ? totalValue : 0);
            System.out.println("GHN returned fee: " + fee);
            return fee;

        } catch (Exception e) {
            System.err.println("Error in fallback method: " + e.getMessage());
            e.printStackTrace();
            return getDefaultShippingFee();
        }
    }

    private String getDistrictIdFromProvince(String provinceCode) {
        try {
            Integer provinceId = convertProvinceCodeToGHNId(provinceCode);
            System.out.println("System Province Code: " + provinceCode + " → GHN Province ID: " + provinceId);

            // GHN API endpoint - URL không có /v2
            String url = ghnApiUrl.replace("/v2", "") + "/master-data/district";
            System.out.println("Request URL: " + url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", ghnToken);
            headers.set("ShopId", ghnShopId);

            // Request body với province_id
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("province_id", provinceId);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                JsonNode data = jsonNode.get("data");

                if (data != null && data.isArray() && data.size() > 0) {
                    // Lấy district đầu tiên
                    return data.get(0).get("DistrictID").asText();
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting district ID: " + e.getMessage());
        }
        return null;
    }

    private String getWardCodeFromCommuneByProvince(String communeCode, Integer provinceId) {
        try {
            System.out.println(" Getting district from province ID: " + provinceId);

            // URL không có /v2
            String districtUrl = ghnApiUrl.replace("/v2", "") + "/master-data/district";
            System.out.println("Request URL: " + districtUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", ghnToken);
            headers.set("ShopId", ghnShopId);

            // Request body với province_id (POST body)
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("province_id", provinceId);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(districtUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                JsonNode data = jsonNode.get("data");

                if (data != null && data.isArray() && data.size() > 0) {
                    // Lấy district ID đầu tiên
                    String districtId = data.get(0).get("DistrictID").asText();
                    System.out.println("Got district ID: " + districtId + ", now fetching wards...");

                    //Gọi ward endpoint với district_id
                    return getWardCodeFromDistrict(communeCode, districtId);
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting ward code by province: " + e.getMessage());
        }
        return null;
    }

    private String getWardCodeFromDistrict(String communeCode, String districtId) {
        try {
            // GHN API endpoint - POST body thay vì query parameter (không có /v2)
            String url = ghnApiUrl.replace("/v2", "") + "/master-data/ward";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", ghnToken);
            headers.set("ShopId", ghnShopId);

            // Request body với district_id
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("district_id", Integer.parseInt(districtId));
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                JsonNode data = jsonNode.get("data");

                if (data != null && data.isArray() && data.size() > 0) {
                    // Lấy ward đầu tiên (thường là phường/xã chính của quận)
                    return data.get(0).get("WardCode").asText();
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting ward code from district: " + e.getMessage());
        }
        return null;
    }

    private String getWardCodeFromCommune(String communeCode, String districtId) {
        try {
            // GHN API endpoint với district_id làm query parameter (không có /v2)
            String url = ghnApiUrl.replace("/v2", "") + "/master-data/ward?district_id=" + districtId;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", ghnToken);
            headers.set("ShopId", ghnShopId);

            // Request body trống
            Map<String, Object> requestBody = new HashMap<>();
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