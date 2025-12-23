package com.example.DATN.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.DATN.dtos.AddressDTO;
import com.example.DATN.dtos.CreateAddressRequestDTO;
import com.example.DATN.models.Address;
import com.example.DATN.models.Commune;
import com.example.DATN.models.Province;
import com.example.DATN.repositories.AddressRepository;
import com.example.DATN.repositories.CommuneRepository;
import com.example.DATN.repositories.ProvinceRepository;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ProvinceRepository provinceRepository;

    @Autowired
    private CommuneRepository communeRepository;

    @Autowired
    private GHNService ghnService;

    // Province methods
    public List<Province> getAllProvinces() {
        return provinceRepository.findAllOrderByName();
    }

    public Optional<Province> getProvinceByCode(String provinceCode) {
        return provinceRepository.findByProvinceCode(provinceCode);
    }

    public List<Province> searchProvincesByName(String name) {
        return provinceRepository.findByProvinceNameContainingIgnoreCase(name);
    }

    // Commune methods
    public List<Commune> getCommunesByProvinceCode(String provinceCode) {
        return communeRepository.findByProvinceCodeOrderByName(provinceCode);
    }

    public Optional<Commune> getCommuneByCode(String communeCode) {
        return communeRepository.findByCommuneCode(communeCode);
    }

    public List<Commune> searchCommunesByName(String name) {
        return communeRepository.findByCommuneNameContainingIgnoreCase(name);
    }

    // Address methods
    public Address createAddress(String specificAddress, String communeCode, String provinceCode) {
        Optional<Province> province = provinceRepository.findByProvinceCode(provinceCode);
        Optional<Commune> commune = communeRepository.findByCommuneCode(communeCode);

        if (province.isPresent() && commune.isPresent()) {
            Address address = Address.builder()
                    .specificAddress(specificAddress)
                    .province(province.get())
                    .commune(commune.get())
                    .isActive(true)
                    .isDefault(false)
                    .build();

            address.generateFullAddress();
            return addressRepository.save(address);
        }

        throw new RuntimeException("Province or Commune not found");
    }

    public Address updateAddress(Integer addressId, String specificAddress, String communeCode, String provinceCode) {
        Optional<Address> existingAddress = addressRepository.findByIdAndIsActiveTrue(addressId);

        if (existingAddress.isPresent()) {
            Address address = existingAddress.get();

            if (specificAddress != null) {
                address.setSpecificAddress(specificAddress);
            }

            if (communeCode != null) {
                Optional<Commune> commune = communeRepository.findByCommuneCode(communeCode);
                commune.ifPresent(address::setCommune);
            }

            if (provinceCode != null) {
                Optional<Province> province = provinceRepository.findByProvinceCode(provinceCode);
                province.ifPresent(address::setProvince);
            }

            address.generateFullAddress();
            return addressRepository.save(address);
        }

        throw new RuntimeException("Address not found");
    }

    public Optional<Address> getAddressById(Integer id) {
        return addressRepository.findByIdAndIsActiveTrue(id);
    }

    public List<Address> getAllActiveAddresses() {
        return addressRepository.findByIsActiveTrue();
    }

    public void deleteAddress(Integer addressId) {
        Optional<Address> address = addressRepository.findByIdAndIsActiveTrue(addressId);
        if (address.isPresent()) {
            Address addr = address.get();
            addr.setIsActive(false);
            addressRepository.save(addr);
        }
    }

    public List<Address> searchAddressesByKeyword(String keyword) {
        return addressRepository.findBySpecificAddressContainingIgnoreCaseAndIsActiveTrue(keyword);
    }

    // Helper method to create sample communes for testing
    public Commune createSampleCommune(String provinceCode, int index) {
        Optional<Province> province = provinceRepository.findByProvinceCode(provinceCode);
        if (province.isPresent()) {
            String communeCode = provinceCode + String.format("%03d", index);
            String communeName = "Phường " + index;

            Commune commune = Commune.builder()
                    .communeCode(communeCode)
                    .communeName(communeName)
                    .communeNameEn("Ward " + index)
                    .communeFullName("Phường " + index)
                    .communeFullNameEn("Ward " + index)
                    .codeName("phuong_" + index + "_" + provinceCode.toLowerCase())
                    .province(province.get())
                    .build();

            return communeRepository.save(commune);
        }
        throw new RuntimeException("Province not found: " + provinceCode);
    }

    // DTO conversion methods
    public AddressDTO convertToDTO(Address address) {
        return AddressDTO.builder()
                .id(address.getId())
                .specificAddress(address.getSpecificAddress())
                .communeCode(address.getCommune() != null ? address.getCommune().getCommuneCode() : null)
                .communeName(address.getCommune() != null ? address.getCommune().getCommuneName() : null)
                .provinceCode(address.getProvince() != null ? address.getProvince().getProvinceCode() : null)
                .provinceName(address.getProvince() != null ? address.getProvince().getProvinceName() : null)
                .fullAddress(address.getFullAddress())
                .isDefault(address.getIsDefault())
                .isActive(address.getIsActive())
                .build();
    }

    public List<AddressDTO> convertToDTOList(List<Address> addresses) {
        return addresses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Address createAddressFromDTO(CreateAddressRequestDTO requestDTO) {
        return createAddress(
                requestDTO.getSpecificAddress(),
                requestDTO.getCommuneCode(),
                requestDTO.getProvinceCode()
        );
    }

    /**
     * Tính phí giao hàng dựa theo tỉnh và phường/xã
     * @param provinceCode Mã tỉnh/thành phố
     * @param communeCode Mã phường/xã (optional)
     * @return Phí giao hàng
     */
    public double calculateShippingFee(String provinceCode, String communeCode) {
        return calculateShippingFee(provinceCode, communeCode, null, null);
    }

    /**
     * Tính phí giao hàng với thông tin chi tiết
     * @param provinceCode Mã tỉnh/thành phố
     * @param communeCode Mã phường/xã
     * @param weight Trọng lượng (gram)
     * @param totalValue Giá trị đơn hàng
     * @return Phí giao hàng
     */
    public double calculateShippingFee(String provinceCode, String communeCode, Integer weight, Double totalValue) {
        try {
            // Sử dụng GHN API để tính phí ship thực tế
            double ghnFee = ghnService.calculateShippingFee(provinceCode, communeCode, weight, totalValue);

            // Log để debug
            System.out.println(String.format("GHN shipping fee calculated: %f VNĐ for province: %s, commune: %s",
                              ghnFee, provinceCode, communeCode));

            return ghnFee;

        } catch (Exception e) {
            System.err.println("Error calling GHN API, falling back to legacy calculation: " + e.getMessage());

            // Fallback về logic cũ nếu GHN API fail
            return calculateShippingFeeLegacy(provinceCode, communeCode);
        }
    }

    /**
     * Logic tính phí cũ (backup khi GHN API không khả dụng)
     */
    private double calculateShippingFeeLegacy(String provinceCode, String communeCode) {
        double baseFee = 30000; // Phí cơ bản

        try {
            Optional<Province> provinceOpt = provinceRepository.findById(provinceCode);
            if (provinceOpt.isEmpty()) {
                return baseFee;
            }

            Province province = provinceOpt.get();
            String provinceName = province.getProvinceName().toLowerCase();

            // Phí giao hàng theo vùng miền
            if (isSpecialCity(provinceName)) {
                return 25000; // Thành phố lớn: Hà Nội, TP.HCM
            } else if (isNorthernProvince(provinceName)) {
                return 35000; // Miền Bắc
            } else if (isCentralProvince(provinceName)) {
                return 40000; // Miền Trung
            } else if (isSouthernProvince(provinceName)) {
                return 35000; // Miền Nam
            }

            return baseFee;

        } catch (Exception e) {
            System.err.println("Error in legacy shipping calculation: " + e.getMessage());
            return baseFee;
        }
    }    private boolean isSpecialCity(String provinceName) {
        return provinceName.contains("hà nội") ||
               provinceName.contains("hồ chí minh") ||
               provinceName.contains("tp hồ chí minh");
    }

    private boolean isNorthernProvince(String provinceName) {
        String[] northernProvinces = {
            "hải phòng", "quảng ninh", "lạng sơn", "cao bằng", "bắc kạn", "thái nguyên",
            "phú thọ", "vĩnh phúc", "bắc giang", "bắc ninh", "hải dương", "hưng yên",
            "nam định", "thái bình", "ninh bình", "hà nam", "hòa bình", "sơn la",
            "điện biên", "lai châu", "lào cai", "yên bái", "tuyên quang", "hà giang"
        };

        for (String province : northernProvinces) {
            if (provinceName.contains(province)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCentralProvince(String provinceName) {
        String[] centralProvinces = {
            "thanh hóa", "nghệ an", "hà tĩnh", "quảng bình", "quảng trị", "thừa thiên huế",
            "đà nẵng", "quảng nam", "quảng ngãi", "bình định", "phú yên", "khánh hòa",
            "ninh thuận", "bình thuận", "kon tum", "gia lai", "đắk lắk", "đắk nông", "lâm đồng"
        };

        for (String province : centralProvinces) {
            if (provinceName.contains(province)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSouthernProvince(String provinceName) {
        String[] southernProvinces = {
            "bình phước", "tây ninh", "bình dương", "đồng nai", "bà rịa vũng tàu",
            "long an", "tiền giang", "bến tre", "trà vinh", "vĩnh long", "đồng tháp",
            "an giang", "kiên giang", "cần thơ", "hậu giang", "sóc trăng", "bạc liêu", "cà mau"
        };

        for (String province : southernProvinces) {
            if (provinceName.contains(province)) {
                return true;
            }
        }
        return false;
    }
}
