package com.example.DATN.utils;

import com.example.DATN.models.Commune;
import com.example.DATN.repositories.CommuneRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class để sync GHN Ward Code từ GHN API vào database
 * Chỉ cần chạy 1 lần để cập nhật dữ liệu
 */
@Component
public class GHNDataSyncUtil {

    @Value("${ghn.api.url:https://dev-online-gateway.ghn.vn/shiip/public-api/v2}")
    private String ghnApiUrl;

    @Value("${ghn.api.token}")
    private String ghnToken;

    @Value("${ghn.shop.id}")
    private String ghnShopId;

    private final CommuneRepository communeRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GHNDataSyncUtil(CommuneRepository communeRepository) {
        this.communeRepository = communeRepository;
    }

    /**
     * Lấy tất cả wards từ GHN và cập nhật vào database
     * Mapping dựa trên tên phường/xã
     */
    public void syncGHNWardCodes(Integer provinceId, Integer districtId) {
        try {
            System.out.println("🔄 Syncing GHN Ward Codes for District ID: " + districtId);

            // Gọi GHN API để lấy danh sách wards
            String url = ghnApiUrl.replace("/v2", "") + "/master-data/ward";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", ghnToken);
            headers.set("ShopId", ghnShopId);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("district_id", districtId);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                JsonNode data = jsonNode.get("data");

                if (data != null && data.isArray()) {
                    int updateCount = 0;

                    for (JsonNode ward : data) {
                        String wardCode = ward.get("WardCode").asText();
                        String wardName = ward.get("WardName").asText();

                        // Tìm commune trong DB dựa trên tên (hoặc code nếu match)
                        // Note: Cần có logic matching phức tạp hơn cho production
                        communeRepository.findAll().stream()
                            .filter(c -> c.getCommuneName().contains(wardName) ||
                                        wardName.contains(c.getCommuneName()))
                            .forEach(commune -> {
                                commune.setGhnWardCode(wardCode);
                                commune.setGhnDistrictId(districtId);
                                communeRepository.save(commune);
                                System.out.println("✅ Updated: " + commune.getCommuneName() + " -> " + wardCode);
                            });

                        updateCount++;
                    }

                    System.out.println("✅ Synced " + updateCount + " wards for District " + districtId);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Error syncing GHN ward codes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lấy District ID từ Province ID
     */
    public void getDistrictsForProvince(Integer provinceId) {
        try {
            String url = ghnApiUrl.replace("/v2", "") + "/master-data/district";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", ghnToken);
            headers.set("ShopId", ghnShopId);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("province_id", provinceId);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                JsonNode data = jsonNode.get("data");

                if (data != null && data.isArray()) {
                    System.out.println("📋 Districts for Province " + provinceId + ":");
                    for (JsonNode district : data) {
                        Integer districtId = district.get("DistrictID").asInt();
                        String districtName = district.get("DistrictName").asText();
                        System.out.println("   - ID: " + districtId + ", Name: " + districtName);

                        // Sync wards cho district này
                        syncGHNWardCodes(provinceId, districtId);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Error getting districts: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
