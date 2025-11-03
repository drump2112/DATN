package com.example.DATN.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.DATN.models.Address;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AddressServiceTest {

    @Autowired
    private AddressService addressService;

    @Test
    public void testCreateAddress() {
        // Test tạo địa chỉ mới
        String specificAddress = "123 Đường ABC";
        String communeCode = "00001"; // Phúc Xá
        String provinceCode = "01"; // Hà Nội

        Address address = addressService.createAddress(specificAddress, communeCode, provinceCode);

        assertNotNull(address);
        assertEquals(specificAddress, address.getSpecificAddress());
        assertEquals(communeCode, address.getCommune().getCommuneCode());
        assertEquals(provinceCode, address.getProvince().getProvinceCode());
        assertTrue(address.getFullAddress().contains(specificAddress));
        assertTrue(address.getFullAddress().contains("Phúc Xá"));
        assertTrue(address.getFullAddress().contains("Hà Nội"));
    }

    @Test
    public void testGetAllProvinces() {
        var provinces = addressService.getAllProvinces();
        assertNotNull(provinces);
        assertFalse(provinces.isEmpty());

        // Kiểm tra có Hà Nội và TP.HCM
        boolean hasHanoi = provinces.stream()
                .anyMatch(p -> "01".equals(p.getProvinceCode()));
        boolean hasHCM = provinces.stream()
                .anyMatch(p -> "79".equals(p.getProvinceCode()));

        assertTrue(hasHanoi, "Should have Hanoi");
        assertTrue(hasHCM, "Should have Ho Chi Minh City");
    }

    @Test
    public void testGetCommunesByProvinceCode() {
        String hanoiProvinceCode = "01";
        var communes = addressService.getCommunesByProvinceCode(hanoiProvinceCode);

        assertNotNull(communes);
        assertFalse(communes.isEmpty());

        // Tất cả communes phải thuộc Hà Nội
        communes.forEach(commune ->
            assertEquals(hanoiProvinceCode, commune.getProvince().getProvinceCode())
        );
    }

    @Test
    public void testSearchProvincesByName() {
        var provinces = addressService.searchProvincesByName("Hà Nội");

        assertNotNull(provinces);
        assertFalse(provinces.isEmpty());

        // Phải có ít nhất 1 kết quả chứa "Hà Nội"
        boolean foundHanoi = provinces.stream()
                .anyMatch(p -> p.getProvinceName().contains("Hà Nội"));
        assertTrue(foundHanoi);
    }

    @Test
    public void testUpdateAddress() {
        // Tạo địa chỉ mới
        Address address = addressService.createAddress(
                "456 Đường XYZ",
                "00001",
                "01"
        );

        // Cập nhật địa chỉ
        Address updatedAddress = addressService.updateAddress(
                address.getId(),
                "789 Đường DEF",
                "00004", // Trúc Bạch
                null // Giữ nguyên tỉnh
        );

        assertNotNull(updatedAddress);
        assertEquals("789 Đường DEF", updatedAddress.getSpecificAddress());
        assertEquals("00004", updatedAddress.getCommune().getCommuneCode());
        assertEquals("01", updatedAddress.getProvince().getProvinceCode());
        assertTrue(updatedAddress.getFullAddress().contains("Trúc Bạch"));
    }

    @Test
    public void testDeleteAddress() {
        // Tạo địa chỉ mới
        Address address = addressService.createAddress(
                "Test Address",
                "00001",
                "01"
        );

        Integer addressId = address.getId();

        // Xóa địa chỉ
        addressService.deleteAddress(addressId);

        // Kiểm tra địa chỉ đã bị đánh dấu không hoạt động
        var deletedAddress = addressService.getAddressById(addressId);
        assertTrue(deletedAddress.isEmpty(), "Address should be soft deleted");
    }

    @Test
    public void testFullAddressGeneration() {
        Address address = addressService.createAddress(
                "123 Nguyễn Du",
                "00001", // Phúc Xá
                "01" // Hà Nội
        );

        String fullAddress = address.getFullAddress();
        assertNotNull(fullAddress);
        assertTrue(fullAddress.contains("123 Nguyễn Du"));
        assertTrue(fullAddress.contains("Phúc Xá"));
        assertTrue(fullAddress.contains("Hà Nội"));

        // Kiểm tra format: "Địa chỉ cụ thể, Phường, Tỉnh"
        String[] parts = fullAddress.split(", ");
        assertEquals(3, parts.length);
    }
}