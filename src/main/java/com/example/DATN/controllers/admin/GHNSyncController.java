package com.example.DATN.controllers.admin;

import com.example.DATN.models.Commune;
import com.example.DATN.repositories.CommuneRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Controller
@RequestMapping("/admin/ghn-sync")
public class GHNSyncController {

    @Value("${ghn.api.url:https://dev-online-gateway.ghn.vn/shiip/public-api/v2}")
    private String ghnApiUrl;

    @Value("${ghn.api.token}")
    private String ghnToken;

    @Value("${ghn.shop.id}")
    private String ghnShopId;

    private final CommuneRepository communeRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GHNSyncController(CommuneRepository communeRepository) {
        this.communeRepository = communeRepository;
    }

    /**
     * Hiển thị trang sync GHN data
     */
    @GetMapping("")
    public String showSyncPage() {
        return "admin/ghn-sync";
    }

    /**
     * API: Lấy danh sách provinces từ GHN
     */
    @GetMapping("/api/provinces")
    @ResponseBody
    public ResponseEntity<?> getGHNProvinces() {
        try {
            String url = ghnApiUrl.replace("/v2", "") + "/master-data/province";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", ghnToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                return ResponseEntity.ok(jsonNode.get("data"));
            }

            return ResponseEntity.badRequest().body(Map.of("error", "Failed to fetch provinces"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * API: Sync GHN Ward Code cho một tỉnh
     */
    @PostMapping("/api/sync-province/{provinceId}")
    @ResponseBody
    public ResponseEntity<?> syncProvinceData(@PathVariable Integer provinceId) {
        try {
            System.out.println("🔄 Starting sync for Province ID: " + provinceId);
            int totalUpdated = 0;

            // Bước 1: Lấy tất cả districts của province
            List<Map<String, Object>> districts = getDistrictsFromGHN(provinceId);
            System.out.println("📋 Found " + districts.size() + " districts");

            for (Map<String, Object> district : districts) {
                Integer districtId = (Integer) district.get("DistrictID");
                String districtName = (String) district.get("DistrictName");

                System.out.println("🔍 Processing District: " + districtName + " (ID: " + districtId + ")");

                // Bước 2: Lấy tất cả wards của district
                List<Map<String, Object>> wards = getWardsFromGHN(districtId);
                System.out.println("   📍 Found " + wards.size() + " wards");

                // Bước 3: Update communes dựa trên tên
                for (Map<String, Object> ward : wards) {
                    String wardCode = (String) ward.get("WardCode");
                    String wardName = (String) ward.get("WardName");

                    // Tìm commune trong DB có tên tương tự
                    List<Commune> matchedCommunes = findMatchingCommunes(wardName);

                    for (Commune commune : matchedCommunes) {
                        commune.setGhnWardCode(wardCode);
                        commune.setGhnDistrictId(districtId);
                        communeRepository.save(commune);

                        System.out.println("   ✅ Updated: " + commune.getCommuneName() +
                                         " -> Ward: " + wardCode + ", District: " + districtId);
                        totalUpdated++;
                    }
                }
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã cập nhật " + totalUpdated + " phường/xã",
                "totalUpdated", totalUpdated
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Lấy danh sách districts từ GHN
     */
    private List<Map<String, Object>> getDistrictsFromGHN(Integer provinceId) {
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

                List<Map<String, Object>> districts = new ArrayList<>();
                if (data != null && data.isArray()) {
                    for (JsonNode district : data) {
                        Map<String, Object> districtMap = new HashMap<>();
                        districtMap.put("DistrictID", district.get("DistrictID").asInt());
                        districtMap.put("DistrictName", district.get("DistrictName").asText());
                        districts.add(districtMap);
                    }
                }
                return districts;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    /**
     * Lấy danh sách wards từ GHN
     */
    private List<Map<String, Object>> getWardsFromGHN(Integer districtId) {
        try {
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

                List<Map<String, Object>> wards = new ArrayList<>();
                if (data != null && data.isArray()) {
                    for (JsonNode ward : data) {
                        Map<String, Object> wardMap = new HashMap<>();
                        wardMap.put("WardCode", ward.get("WardCode").asText());
                        wardMap.put("WardName", ward.get("WardName").asText());
                        wards.add(wardMap);
                    }
                }
                return wards;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    /**
     * Tìm communes trong DB khớp với tên ward từ GHN
     */
    private List<Commune> findMatchingCommunes(String wardName) {
        List<Commune> allCommunes = communeRepository.findAll();
        List<Commune> matches = new ArrayList<>();

        // Normalize tên để so sánh
        String normalizedWardName = normalizeVietnameseName(wardName);

        for (Commune commune : allCommunes) {
            String communeName = normalizeVietnameseName(commune.getCommuneName());
            String communeFullName = normalizeVietnameseName(commune.getCommuneFullName());

            // So sánh chính xác hoặc chứa tên
            if (communeName.equals(normalizedWardName) ||
                communeFullName.contains(normalizedWardName) ||
                normalizedWardName.contains(communeName)) {
                matches.add(commune);
            }
        }

        return matches;
    }

    /**
     * Chuẩn hóa tên tiếng Việt để so sánh
     */
    private String normalizeVietnameseName(String name) {
        if (name == null) return "";

        return name.toLowerCase()
                   .replaceAll("phường ", "")
                   .replaceAll("xã ", "")
                   .replaceAll("thị trấn ", "")
                   .trim();
    }
}
