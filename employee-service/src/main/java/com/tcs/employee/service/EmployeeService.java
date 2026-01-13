package com.tcs.employee.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tcs.employee.dto.EmployeeCreateRequest;
import com.tcs.employee.dto.EmployeeResponseDto;
import com.tcs.employee.dto.EmployeeUpdateRequest;
import com.tcs.employee.entity.EmployeeProfile;

public interface EmployeeService {

	EmployeeResponseDto createEmployee(EmployeeCreateRequest req);

	EmployeeResponseDto updateEmployee(Long id, EmployeeUpdateRequest req);

	EmployeeResponseDto getById(Long id);

	Page<EmployeeResponseDto> list(String skill, Integer minExp, String status, String location,
			Pageable pageable);

	boolean claimForAllocation(Long employeeId, Long projectId);

	void releaseAllocation(Long employeeId);

	List<EmployeeResponseDto> getByIds(List<Long> ids);

	void deleteEmployee(Long id);
}
