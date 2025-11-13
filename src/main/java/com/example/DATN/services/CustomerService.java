package com.example.DATN.services;

import java.util.List;

import com.example.DATN.dtos.CategoryDTO;
import com.example.DATN.dtos.CustomerDTO;
import com.example.DATN.request.CustomerRequest;
import com.example.DATN.request.EmployeeRequest;

public interface CustomerService {

  boolean addCustomer(CustomerRequest customerRequest);

  boolean addQuickCustomer(String fullName, String phone);

   List<CustomerDTO> getCustomers(String keyword);
}
