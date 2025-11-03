package com.example.DATN.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.DATN.models.Commune;

@Repository
public interface CommuneRepository extends JpaRepository<Commune, String> {

    Optional<Commune> findByCommuneCode(String communeCode);

    List<Commune> findByProvinceProvinceCode(String provinceCode);

    @Query("SELECT c FROM Commune c WHERE c.province.provinceCode = :provinceCode ORDER BY c.communeName")
    List<Commune> findByProvinceCodeOrderByName(@Param("provinceCode") String provinceCode);

    List<Commune> findByCommuneNameContainingIgnoreCase(String communeName);

    @Query("SELECT c FROM Commune c WHERE c.codeName = :codeName")
    Optional<Commune> findByCodeName(@Param("codeName") String codeName);
}