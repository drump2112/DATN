package com.example.DATN.services;

import com.example.DATN.request.CustomerRequest;
import com.example.DATN.request.EmployeeRequest;

public interface CustomerService {

  boolean addCustomer(CustomerRequest customerRequest);

}
