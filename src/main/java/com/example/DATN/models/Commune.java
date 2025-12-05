package com.example.DATN.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "Communes")
@Builder(toBuilder = true)
public class Commune {

    @Id
    @Column(name = "CommuneCode", length = 5)
    private String communeCode;

    @Column(name = "CommuneName", nullable = false, length = 100)
    private String communeName;

    @Column(name = "CommuneNameEn", length = 100)
    private String communeNameEn;

    @Column(name = "CommuneFullName", length = 150)
    private String communeFullName;

    @Column(name = "CommuneFullNameEn", length = 150)
    private String communeFullNameEn;

    @Column(name = "CodeName", length = 50)
    private String codeName;

    @Column(name = "GHNWardCode", length = 20)
    private String ghnWardCode;

    @Column(name = "GHNDistrictId")
    private Integer ghnDistrictId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ProvinceCode", referencedColumnName = "ProvinceCode")
    private Province province;
}