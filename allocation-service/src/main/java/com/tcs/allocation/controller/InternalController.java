package com.tcs.allocation.controller;

import com.tcs.allocation.dto.*;
import com.tcs.allocation.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalController {

    private final CandidateService candidateService;
    private final AllocationService allocationService;

    @GetMapping("/candidates/request/{requestId}")
    public ResponseEntity<List<CandidateDto>> listCandidates(@PathVariable Long requestId) {
        return ResponseEntity.ok(candidateService.listByRequest(requestId));
    }

    @GetMapping("/allocations/request/{requestId}")
    public ResponseEntity<List<AllocationAttemptDto>> listAllocations(@PathVariable Long requestId) {
        return ResponseEntity.ok(allocationService.listAllocationsByRequest(requestId));
    }
}
