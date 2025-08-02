package com.example.DATN.services;

import com.example.DATN.dtos.UserDTO;
import com.example.DATN.request.EmployeeRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

	Page<UserDTO> getAllCustomer(int page, int size);

	Page<UserDTO> getAllEmployee(int page, int size);

	public long countUsersByRoles(String keyword, Integer... roleIds);

	boolean toggleStatus(Integer id);

	boolean addEmployee(EmployeeRequest employeeRequest);

	boolean updateEmployee(Integer id, EmployeeRequest employeeRequest);

	Page<UserDTO> searchUsers(String keyword, Boolean isActive, Pageable pageable);
}
