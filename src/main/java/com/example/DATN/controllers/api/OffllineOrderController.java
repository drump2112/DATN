package com.example.DATN.controllers.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.DATN.dtos.CategoryDTO;
import com.example.DATN.dtos.CustomerDTO;
import com.example.DATN.dtos.VoucherDTO;
import com.example.DATN.models.Voucher;
import com.example.DATN.repositories.VoucherRepository;
import com.example.DATN.services.CustomerService;

@Controller
@RequestMapping("/seller/buyatthecounter")
public class OffllineOrderController {

	@Autowired
	private CustomerService customerService;
	@Autowired
	private VoucherRepository voucherRepository;

	@GetMapping("/")
	public String getPageCounterSale() {
		return "admin/buyatthecounter/buyatthecounter";
	}

	@ResponseBody
	@GetMapping("/discounts")
	public List<Voucher> getAllVoucher() {
		return voucherRepository.findAll();
	}

	@GetMapping("/select2")
	@ResponseBody
	public List<Map<String, Object>> getCustomerForSelect2(@RequestParam(required = false) String q) {
		List<CustomerDTO> customers = customerService.getCustomers(q);

		return customers.stream()
				.map(user -> {
					Map<String, Object> item = new HashMap<>();
					item.put("id", user.getId());
					item.put("text", user.getFullName() + " - " + user.getPhone());
					return item;
				})
				.collect(Collectors.toList());
	}

}
