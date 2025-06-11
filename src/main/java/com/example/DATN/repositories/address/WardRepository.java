package com.example.DATN.repositories.address;

import java.util.List;

import com.example.DATN.models.address.Ward;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WardRepository extends JpaRepository<Ward, String> {

	List<Ward> findByDistrict_Code(String districtCode);

}
