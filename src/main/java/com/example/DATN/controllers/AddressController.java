package com.example.DATN.controllers;

import java.util.List;

import com.example.DATN.dtos.SimpleDTO;
import com.example.DATN.repositories.address.DistrictRepository;
import com.example.DATN.repositories.address.ProvinceRepository;
import com.example.DATN.repositories.address.WardRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AddressController {

	@Autowired
	private ProvinceRepository provinceRepository;
	@Autowired
	private DistrictRepository districtRepository;
	@Autowired
	private WardRepository wardRepository;

	@GetMapping("/provinces")
	public List<SimpleDTO> getProvinces() {
		return provinceRepository.findAll().stream()
				.map(p -> new SimpleDTO(p.getCode(), p.getName()))
				.toList();
	}

	@GetMapping("/districts")
	public List<SimpleDTO> getDistricts(@RequestParam String provinceCode) {
		return districtRepository.findByProvince_Code(provinceCode).stream()
				.map(d -> new SimpleDTO(d.getCode(), d.getName()))
				.toList();
	}

	@GetMapping("/wards")
	public List<SimpleDTO> getWards(@RequestParam String districtCode) {
		return wardRepository.findByDistrict_Code(districtCode).stream()
				.map(w -> new SimpleDTO(w.getCode(), w.getName()))
				.toList();
	}

}
