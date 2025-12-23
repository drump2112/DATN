package com.example.DATN.models;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "Provinces")
@Builder(toBuilder = true)
public class Province {

    @Id
    @Column(name = "ProvinceCode", length = 2)
    private String provinceCode;

    @Column(name = "ProvinceName", nullable = false, length = 100)
    private String provinceName;

    @Column(name = "ProvinceNameEn", length = 100)
    private String provinceNameEn;

    @Column(name = "ProvinceFullName", length = 150)
    private String provinceFullName;

    @Column(name = "ProvinceFullNameEn", length = 150)
    private String provinceFullNameEn;

    @Column(name = "CodeName", length = 50)
    private String codeName;

    @OneToMany(mappedBy = "province", cascade = CascadeType.ALL)
    private List<Commune> communes;
}