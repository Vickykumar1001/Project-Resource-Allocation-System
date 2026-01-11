package com.tcs.allocation.client;

import com.tcs.allocation.dto.CandidateDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Minimal endpoints expected from Employee Service.
 * Adapt if your Employee service uses different paths/DTOs.
 */
@FeignClient(name = "employee-service", configuration = com.tcs.allocation.config.FeignConfig.class)
public interface EmployeeClient {

    // fetch employee details by ids (batch)
    @GetMapping("/api/internal/employees")
    List<Map<String,Object>> getEmployeesByIds(@RequestParam("ids") String ids);

    // search available employees by skill, exp, page
    @GetMapping("/api/internal/employees/search")
    List<Map<String,Object>> searchEmployees(@RequestParam("skill") String skill,
                                             @RequestParam(value="minExp", required=false) Integer minExp,
                                             @RequestParam(value="page", defaultValue="0") int page,
                                             @RequestParam(value="size", defaultValue="100") int size);

    // claim employee for allocation -> returns 200 OK on success, 409 on conflict
    @PostMapping("/api/internal/employees/{id}/claim")
    void claimEmployee(@PathVariable("id") Long id, @RequestParam("projectId") Long projectId);

    @PostMapping("/api/internal/employees/{id}/release")
    void releaseEmployee(@PathVariable("id") Long id);
}
