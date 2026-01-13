package com.tcs.allocation.client;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tcs.allocation.dto.EmployeeResponseDto;

@FeignClient(name = "employee-service", configuration = com.tcs.allocation.config.FeignConfig.class)
public interface EmployeeClient {

    @GetMapping("/employees/internal")
    List<Map<String,Object>> getEmployeesByIds(@RequestParam("ids") String ids);

    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeResponseDto>> list(
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) Integer minExp,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );
    
    @PostMapping("/employees/internal/{id}/claim")
    void claimEmployee(@PathVariable("id") Long id, @RequestParam("projectId") Long projectId);

    @PostMapping("/employees/internal/{id}/release")
    void releaseEmployee(@PathVariable("id") Long id);
}
