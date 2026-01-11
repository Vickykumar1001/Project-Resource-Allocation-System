package com.tcs.allocation.controller;

import com.tcs.allocation.dto.*;
import com.tcs.allocation.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateService candidateService;
    private final AllocationService allocationService;

    @GetMapping("/request/{requestId}")
    public ResponseEntity<List<CandidateDto>> listByRequest(@PathVariable Long requestId) {
        return ResponseEntity.ok(candidateService.listByRequest(requestId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidateDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(candidateService.getCandidate(id));
    }

    @PostMapping("/{id}/schedule-interview")
    public ResponseEntity<InterviewDto> schedule(@PathVariable Long id, @RequestBody InterviewDto dto) {
        dto.setCandidateId(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(candidateService.scheduleInterview(id, dto));
    }

    @PutMapping("/interviews/{id}/result")
    public ResponseEntity<InterviewDto> interviewResult(@PathVariable Long id, @RequestBody InterviewDto dto) {
        return ResponseEntity.ok(candidateService.recordInterviewResult(id, dto));
    }

    @PostMapping("/{id}/allocate")
    public ResponseEntity<AllocationAttemptDto> allocate(@PathVariable Long id, @RequestParam Long allocatedByUserId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(allocationService.allocateCandidate(id, allocatedByUserId));
    }
}
