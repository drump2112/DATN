package com.example.DATN.repositories.address;

import java.util.List;

import com.example.DATN.models.address.District;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DistrictRepository extends JpaRepository<District, String> {

	List<District> findByProvince_Code(String provinceCode);

}
