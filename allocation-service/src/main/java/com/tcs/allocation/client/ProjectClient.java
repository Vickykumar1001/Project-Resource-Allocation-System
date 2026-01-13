package com.tcs.allocation.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tcs.allocation.dto.AllocationDto;
import com.tcs.allocation.dto.ResourceRequestResponseDto;

@FeignClient(name = "project-service", configuration = com.tcs.allocation.config.FeignConfig.class)
public interface ProjectClient {

	@PostMapping("/project/internal/resource-requests/{id}/allocate")
	public ResponseEntity<AllocationDto> allocate(@PathVariable Long id, @RequestParam Long employeeId,
			@RequestParam Long allocatedByUserId);

	@PostMapping("/project/internal/allocations/{id}/release")
	public ResponseEntity<Void> release(@PathVariable Long id);

	@GetMapping("/project/internal/allocations/{id}")
	public ResponseEntity<AllocationDto> getAllocation(@PathVariable Long id);

	@GetMapping("/project/internal/resource-requests/{id}")
    public ResponseEntity<ResourceRequestResponseDto> getRequest(@PathVariable Long id);
}
