package com.tcs.employee.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.tcs.employee.dto.EmployeeCreateRequest;
import com.tcs.employee.exception.BadRequestException;
import com.tcs.employee.repository.EmployeeProfileRepository;

class EmployeeServiceImplTest {
	 @Mock
	    private EmployeeProfileRepository profileRepo;

	   
	    @InjectMocks
	    private EmployeeService employeeService; 

	    @BeforeEach
	    void setUp() {
	        employeeService = Mockito.spy(employeeService);
	    }

	@Test
	void testCreateEmployee() {
		 EmployeeCreateRequest req = new EmployeeCreateRequest();

	        // Act & Assert
	        BadRequestException ex = assertThrows(BadRequestException.class,
	                () -> employeeService.createEmployee(req));
	        assertTrue(ex.getMessage().contains("No user exists for provided token"));
	        // repo should never be touched
	        verify(profileRepo, never()).findByUserIdAndDeletedFalse(any());
	        verify(profileRepo, never()).save(any());
	}

}
