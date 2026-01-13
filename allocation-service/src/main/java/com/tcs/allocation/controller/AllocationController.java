package com.tcs.allocation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tcs.allocation.dto.AllocationDto;
import com.tcs.allocation.dto.CandidateDto;
import com.tcs.allocation.service.AllocationService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/allocation")
public class AllocationController {

    private final AllocationService allocationService;


    @GetMapping("/{reqId}/suggestCandidates")
    public ResponseEntity<List<CandidateDto>> suggestCandidates(@PathVariable Long reqId) {
        List<CandidateDto> candidates = allocationService.suggestCandidates(reqId);
        return ResponseEntity.ok(candidates);
    }

    @PostMapping("/{reqId}/allocate")
    public ResponseEntity<AllocationDto> allocateEmployee(
            @PathVariable Long reqId,
            @RequestParam Long employeeId,
            @RequestParam Long allocatedByUserId) {

        AllocationDto allocation = allocationService.allocate(reqId, employeeId, allocatedByUserId);
        return ResponseEntity.ok(allocation);
    }

    @PostMapping("/release/{allocationId}")
    public ResponseEntity<AllocationDto> releaseEmployee(@PathVariable Long allocationId) {
        AllocationDto allocation = allocationService.release(allocationId);
        return ResponseEntity.ok(allocation);
    }
}
