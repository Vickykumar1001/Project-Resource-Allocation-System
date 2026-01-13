package com.tcs.project.controller;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tcs.project.dto.AllocationDto;
import com.tcs.project.dto.CandidateSuggestionDto;
import com.tcs.project.dto.ResourceRequestResponseDto;
import com.tcs.project.entity.ResourceRequest;
import com.tcs.project.service.ProjectService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/project/internal")
@RequiredArgsConstructor
public class InternalController {

    private final ProjectService service;

    @GetMapping("/resource-requests")
    public ResponseEntity<Page<ResourceRequestResponseDto>> listRequests(
            @RequestParam(required = false) ResourceRequest.RequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<ResourceRequestResponseDto> p = service.listResourceRequests(status, pageable);
        return ResponseEntity.ok(p);
    }

    @GetMapping("/resource-requests/{id}")
    public ResponseEntity<ResourceRequestResponseDto> getRequest(@PathVariable Long id) {
        return ResponseEntity.ok(service.getRequest(id));
    }

    @PostMapping("/resource-requests/{id}/suggest")
    public ResponseEntity<CandidateSuggestionDto> suggest(@PathVariable Long id, @RequestBody CandidateSuggestionDto dto) {
        dto.setResourceRequestId(id);
        CandidateSuggestionDto created = service.suggestCandidate(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/resource-requests/{id}/suggestions")
    public ResponseEntity<List<CandidateSuggestionDto>> listSuggestions(@PathVariable Long id) {
        return ResponseEntity.ok(service.listSuggestions(id));
    }

    @PutMapping("/suggestions/{id}/review")
    public ResponseEntity<CandidateSuggestionDto> reviewSuggestion(@PathVariable Long id,
                                                                   @RequestParam boolean approve,
                                                                   @RequestParam(required = false) String feedback) {
        CandidateSuggestionDto updated = service.reviewSuggestion(id, approve, feedback);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/resource-requests/{id}/allocate")
    public ResponseEntity<AllocationDto> allocate(@PathVariable Long id,
                                                  @RequestParam Long employeeId,
                                                  @RequestParam Long allocatedByUserId) {
        AllocationDto a = service.allocateEmployee(id, employeeId, allocatedByUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(a);
    }

	@GetMapping("allocations/{id}")
	public ResponseEntity<AllocationDto> getAllocation(@PathVariable Long id){
    	AllocationDto a = service.getAllocation(id);
    	return ResponseEntity.status(HttpStatus.OK).body(a);
    }

    @GetMapping("/allocations/request/{id}")
    public ResponseEntity<List<AllocationDto>> listAllocationsForRequest(@PathVariable Long id) {
        return ResponseEntity.ok(service.listAllocationsByRequest(id));
    }

    @PostMapping("/allocations/{id}/release")
    public ResponseEntity<Void> release(@PathVariable Long id) {
        service.releaseAllocation(id);
        return ResponseEntity.ok().build();
    }
}
