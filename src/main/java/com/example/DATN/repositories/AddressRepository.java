package com.example.DATN.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.DATN.models.Address;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {

    List<Address> findByIsActiveTrue();

    @Query("SELECT a FROM Address a WHERE a.commune.communeCode = :communeCode AND a.isActive = true")
    List<Address> findByCommuneCode(@Param("communeCode") String communeCode);

    @Query("SELECT a FROM Address a WHERE a.province.provinceCode = :provinceCode AND a.isActive = true")
    List<Address> findByProvinceCode(@Param("provinceCode") String provinceCode);

    @Query("SELECT a FROM Address a WHERE a.fullAddress LIKE %:keyword% AND a.isActive = true")
    List<Address> findByFullAddressContaining(@Param("keyword") String keyword);

    @Query("SELECT a FROM Address a WHERE UPPER(a.specificAddress) LIKE UPPER(CONCAT('%', :keyword, '%')) AND a.isActive = true")
    List<Address> findBySpecificAddressContainingIgnoreCaseAndIsActiveTrue(@Param("keyword") String keyword);

    Optional<Address> findByIdAndIsActiveTrue(Integer id);
}