package com.example.DATN.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.DATN.dtos.ShippingInfoDTO;
import com.example.DATN.models.Address;
import com.example.DATN.models.User;
import com.example.DATN.repositories.AddressRepository;
import com.example.DATN.repositories.UserRepository;

@Service
public class UserShippingService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    /**
     * Lấy thông tin giao hàng của người dùng (bao gồm mã tỉnh, mã phường/xã)
     * Sử dụng cho API giao hàng
     */
    public ShippingInfoDTO getUserShippingInfo(Integer userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy người dùng với ID: " + userId);
        }

        User user = userOpt.get();
        Address address = user.getAddress();

        if (address == null) {
            return ShippingInfoDTO.builder()
                    .userId(user.getId())
                    .userFullName(user.getFullName())
                    .userPhone(user.getPhone())
                    .build();
        }

        return ShippingInfoDTO.builder()
                .userId(user.getId())
                .userFullName(user.getFullName())
                .userPhone(user.getPhone())
                .addressId(address.getId())
                .specificAddress(address.getSpecificAddress())
                .fullAddress(address.getFullAddress())
                .provinceCode(address.getProvince() != null ? address.getProvince().getProvinceCode() : null)
                .provinceName(address.getProvince() != null ? address.getProvince().getProvinceName() : null)
                .communeCode(address.getCommune() != null ? address.getCommune().getCommuneCode() : null)
                .communeName(address.getCommune() != null ? address.getCommune().getCommuneName() : null)
                .provinceFullName(address.getProvince() != null ? address.getProvince().getProvinceFullName() : null)
                .communeFullName(address.getCommune() != null ? address.getCommune().getCommuneFullName() : null)
                .build();
    }

    /**
     * Cập nhật địa chỉ cho người dùng
     */
    public boolean updateUserAddress(Integer userId, Integer addressId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return false;
            }

            User user = userOpt.get();

            if (addressId == null) {
                user.setAddress(null);
            } else {
                Optional<Address> addressOpt = addressRepository.findByIdAndIsActiveTrue(addressId);
                if (addressOpt.isEmpty()) {
                    return false;
                }
                user.setAddress(addressOpt.get());
            }

            userRepository.save(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy mã tỉnh của người dùng (cho API giao hàng)
     */
    public String getUserProvinceCode(Integer userId) {
        ShippingInfoDTO shippingInfo = getUserShippingInfo(userId);
        return shippingInfo.getProvinceCode();
    }

    /**
     * Lấy mã phường/xã của người dùng (cho API giao hàng)
     */
    public String getUserCommuneCode(Integer userId) {
        ShippingInfoDTO shippingInfo = getUserShippingInfo(userId);
        return shippingInfo.getCommuneCode();
    }

    /**
     * Kiểm tra xem người dùng có địa chỉ đầy đủ không (cần cho giao hàng)
     */
    public boolean hasCompleteAddress(Integer userId) {
        ShippingInfoDTO shippingInfo = getUserShippingInfo(userId);
        return shippingInfo.getProvinceCode() != null &&
               shippingInfo.getCommuneCode() != null &&
               shippingInfo.getSpecificAddress() != null &&
               !shippingInfo.getSpecificAddress().trim().isEmpty();
    }
}