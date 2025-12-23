package com.example.DATN.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.DATN.models.Province;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, String> {

    Optional<Province> findByProvinceCode(String provinceCode);

    List<Province> findByProvinceNameContainingIgnoreCase(String provinceName);

    @Query("SELECT p FROM Province p ORDER BY p.provinceName")
    List<Province> findAllOrderByName();

    @Query("SELECT p FROM Province p WHERE p.codeName = :codeName")
    Optional<Province> findByCodeName(@Param("codeName") String codeName);
}