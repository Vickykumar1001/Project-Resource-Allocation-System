package com.tcs.employee.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tcs.employee.dto.EmployeeResponseDto;
import com.tcs.employee.service.EmployeeService;

import lombok.RequiredArgsConstructor;

/**
 * Internal endpoints for other microservices to call.
 * Protect these in production (require service-to-service auth).
 */
@RestController
@RequestMapping("/api/internal/employees")
@RequiredArgsConstructor
public class InternalEmployeeController {

    private final EmployeeService service;

    // GET /api/internal/employees?ids=1,2,3
    @GetMapping
    public ResponseEntity<?> getByIds(@RequestParam(required = false) String ids,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        if (ids != null && !ids.isBlank()) {
            List<Long> parsed = Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            List<EmployeeResponseDto> list = service.getByIds(parsed);
            return ResponseEntity.ok(list);
        } else {
            // forward to public paginated endpoint by building request parameters - reuse service.list with null filters
            var p = service.list(null, null, null, null, PageRequest.of(page, size, Sort.by("id").ascending()));
            return ResponseEntity.ok(p);
        }
    }

    // POST /api/internal/employees/{id}/claim?projectId=123
    @PostMapping("/{id}/claim")
    public ResponseEntity<?> claim(@PathVariable Long id, @RequestParam Long projectId) {
        boolean ok = service.claimForAllocation(id, projectId);
        if (ok) return ResponseEntity.ok().build();
        else return ResponseEntity.status(409).body("Employee not available for allocation");
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<?> release(@PathVariable Long id) {
        service.releaseAllocation(id);
        return ResponseEntity.ok().build();
    }
}
