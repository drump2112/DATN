package com.example.DATN.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "Addresses")
@Builder(toBuilder = true)
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "SpecificAddress", columnDefinition = "nvarchar(500)")
    private String specificAddress; // Số nhà, tên đường

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CommuneCode", referencedColumnName = "CommuneCode")
    private Commune commune;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ProvinceCode", referencedColumnName = "ProvinceCode")
    private Province province;

    @Column(name = "FullAddress", columnDefinition = "nvarchar(1000)")
    private String fullAddress;

    @Column(name = "IsDefault")
    @Builder.Default
    private Boolean isDefault = false; // Địa chỉ mặc định

    @Column(name = "IsActive")
    @Builder.Default
    private Boolean isActive = true;

    // Phương thức để tạo địa chỉ đầy đủ
    public String generateFullAddress() {
        StringBuilder fullAddr = new StringBuilder();

        if (specificAddress != null && !specificAddress.trim().isEmpty()) {
            fullAddr.append(specificAddress);
        }

        if (commune != null) {
            if (fullAddr.length() > 0) fullAddr.append(", ");
            fullAddr.append(commune.getCommuneFullName());
        }

        if (province != null) {
            if (fullAddr.length() > 0) fullAddr.append(", ");
            fullAddr.append(province.getProvinceFullName());
        }

        this.fullAddress = fullAddr.toString();
        return this.fullAddress;
    }
}