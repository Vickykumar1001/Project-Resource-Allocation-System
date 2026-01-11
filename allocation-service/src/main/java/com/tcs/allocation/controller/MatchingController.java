package com.tcs.allocation.controller;

import com.tcs.allocation.dto.*;
import com.tcs.allocation.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matching-jobs")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    @PostMapping
    public ResponseEntity<MatchingJobDto> create(@RequestBody MatchingJobCreateDto dto) {
        MatchingJobDto created = matchingService.createJob(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<MatchingJobDto>> list(@RequestParam(required=false) Long resourceRequestId,
                                                     @RequestParam(defaultValue="0") int page,
                                                     @RequestParam(defaultValue="20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(matchingService.listJobs(resourceRequestId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchingJobDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(matchingService.getJob(id));
    }

    @PostMapping("/{id}/run")
    public ResponseEntity<Void> run(@PathVariable Long id) {
        matchingService.runJob(id, null);
        return ResponseEntity.ok().build();
    }
}
