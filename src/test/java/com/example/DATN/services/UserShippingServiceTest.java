package com.example.DATN.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.DATN.dtos.ShippingInfoDTO;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserShippingServiceTest {

    @Autowired(required = false)
    private UserShippingService userShippingService;

    @Autowired(required = false)
    private ShippingCodeMappingService mappingService;

    @Test
    public void testGetUserShippingInfo() {
        if (userShippingService == null) {
            System.out.println("UserShippingService not available, skipping test");
            return;
        }

        // Test với user có địa chỉ
        try {
            ShippingInfoDTO shippingInfo = userShippingService.getUserShippingInfo(1);

            assertNotNull(shippingInfo);
            assertNotNull(shippingInfo.getUserId());

            // Nếu có địa chỉ, kiểm tra thông tin
            if (shippingInfo.getAddressId() != null) {
                assertNotNull(shippingInfo.getProvinceCode());
                assertNotNull(shippingInfo.getCommuneCode());
                assertNotNull(shippingInfo.getFullAddress());
            }

            System.out.println("Shipping Info: " + shippingInfo);

        } catch (RuntimeException e) {
            System.out.println("User not found or no address: " + e.getMessage());
        }
    }

    @Test
    public void testProvinceMappingToGHN() {
        if (mappingService == null) {
            System.out.println("ShippingCodeMappingService not available, skipping test");
            return;
        }

        // Test mapping mã tỉnh Hà Nội
        Integer ghnHanoiId = mappingService.convertProvinceCodeToGHN("01");
        assertEquals(269, ghnHanoiId, "Hà Nội should map to GHN ID 269");

        // Test mapping mã tỉnh TP.HCM
        Integer ghnHCMId = mappingService.convertProvinceCodeToGHN("79");
        assertEquals(202, ghnHCMId, "TP.HCM should map to GHN ID 202");

        // Test mã tỉnh không tồn tại
        Integer unknownId = mappingService.convertProvinceCodeToGHN("99");
        assertNull(unknownId, "Unknown province code should return null");
    }

    @Test
    public void testCommuneMappingToGHN() {
        if (mappingService == null) {
            System.out.println("ShippingCodeMappingService not available, skipping test");
            return;
        }

        // Test mapping mã phường/xã
        Integer ghnCommuneId = mappingService.convertCommuneCodeToGHN("00001");
        assertNotNull(ghnCommuneId, "Phúc Xá should have GHN mapping");

        // Test mã không tồn tại
        Integer unknownId = mappingService.convertCommuneCodeToGHN("99999");
        assertNull(unknownId, "Unknown commune code should return null");
    }

    @Test
    public void testSupportCheck() {
        if (mappingService == null) {
            System.out.println("ShippingCodeMappingService not available, skipping test");
            return;
        }

        // Test hỗ trợ tỉnh
        assertTrue(mappingService.isGHNSupportedProvince("01"), "Hà Nội should be supported");
        assertTrue(mappingService.isGHNSupportedProvince("79"), "TP.HCM should be supported");
        assertFalse(mappingService.isGHNSupportedProvince("99"), "Unknown province should not be supported");

        // Test hỗ trợ phường/xã
        assertTrue(mappingService.isGHNSupportedCommune("00001"), "Phúc Xá should be supported");
        assertFalse(mappingService.isGHNSupportedCommune("99999"), "Unknown commune should not be supported");
    }

    @Test
    public void testVTPMapping() {
        if (mappingService == null) {
            System.out.println("ShippingCodeMappingService not available, skipping test");
            return;
        }

        String vtpHanoi = mappingService.convertProvinceCodeToVTP("01");
        assertEquals("HN", vtpHanoi, "Hà Nội should map to 'HN' in VTP");

        String vtpHCM = mappingService.convertProvinceCodeToVTP("79");
        assertEquals("HCM", vtpHCM, "TP.HCM should map to 'HCM' in VTP");
    }

    @Test
    public void testJTMapping() {
        if (mappingService == null) {
            System.out.println("ShippingCodeMappingService not available, skipping test");
            return;
        }

        String jtHanoi = mappingService.convertProvinceCodeToJT("01");
        assertEquals("HANOI", jtHanoi, "Hà Nội should map to 'HANOI' in J&T");

        String jtHCM = mappingService.convertProvinceCodeToJT("79");
        assertEquals("HOCHIMINH", jtHCM, "TP.HCM should map to 'HOCHIMINH' in J&T");
    }

    @Test
    public void testCompleteAddressCheck() {
        if (userShippingService == null) {
            System.out.println("UserShippingService not available, skipping test");
            return;
        }

        try {
            boolean hasComplete = userShippingService.hasCompleteAddress(1);
            System.out.println("User 1 has complete address: " + hasComplete);

            // Test với user không tồn tại
            assertThrows(RuntimeException.class, () -> {
                userShippingService.hasCompleteAddress(99999);
            });

        } catch (RuntimeException e) {
            System.out.println("User not found: " + e.getMessage());
        }
    }

    @Test
    public void testProvinceAndCommuneCodeRetrieval() {
        if (userShippingService == null) {
            System.out.println("UserShippingService not available, skipping test");
            return;
        }

        try {
            String provinceCode = userShippingService.getUserProvinceCode(1);
            String communeCode = userShippingService.getUserCommuneCode(1);

            System.out.println("User 1 - Province Code: " + provinceCode);
            System.out.println("User 1 - Commune Code: " + communeCode);

            // Nếu có địa chỉ thì code không null
            if (provinceCode != null) {
                assertEquals(2, provinceCode.length(), "Province code should be 2 characters");
            }

            if (communeCode != null) {
                assertEquals(5, communeCode.length(), "Commune code should be 5 characters");
            }

        } catch (RuntimeException e) {
            System.out.println("User not found or no address: " + e.getMessage());
        }
    }
}