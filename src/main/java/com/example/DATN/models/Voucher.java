package com.example.DATN.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "Voucher")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "Name", length = 100)
    private String name;

    @Column(name = "Code", length = 50, unique = true)
    private String code;

    @Column(name = "DiscountType", length = 10)
    private String discountType;

    @Column(name = "DiscountValue", precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "MinOrderAmount", precision = 10, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "MaxDiscountValue", precision = 10, scale = 2)
    private BigDecimal maxDiscountValue;

    @Column(name = "StartDate")
    private LocalDateTime startDate;

    @Column(name = "EndDate")
    private LocalDateTime endDate;

    @Column(name = "Quantity")
    private Integer quantity;

    @Column(name = "IsActive")
    private Boolean isActive;

    @OneToMany(mappedBy = "voucher", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Order> orders;
}
