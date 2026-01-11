package com.tcs.employee.controller;

import com.tcs.employee.dto.*;
import com.tcs.employee.entity.EmployeeProfile;
import com.tcs.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService service;

    @PostMapping
    public ResponseEntity<EmployeeResponseDto> create(@Validated @RequestBody EmployeeCreateRequest req) {
        EmployeeResponseDto dto = service.createEmployee(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> update(@PathVariable Long id, @RequestBody EmployeeUpdateRequest req) {
        EmployeeResponseDto dto = service.updateEmployee(id, req);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> getById(@PathVariable Long id) {
        EmployeeResponseDto dto = service.getById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeResponseDto>> list(
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) Integer minExp,
            @RequestParam(required = false) EmployeeProfile.CurrentStatus status,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<EmployeeResponseDto> p = service.list(skill, minExp, status, location, pageable);
        return ResponseEntity.ok(p);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<EmployeeResponseDto> setStatus(@PathVariable Long id, @RequestParam EmployeeProfile.CurrentStatus status) {
        EmployeeResponseDto dto = service.setStatus(id, status);
        return ResponseEntity.ok(dto);
    }
}
