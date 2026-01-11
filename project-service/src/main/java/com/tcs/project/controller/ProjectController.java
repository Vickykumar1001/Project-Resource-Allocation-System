package com.tcs.project.controller;

import com.tcs.project.dto.*;
import com.tcs.project.entity.ResourceRequest;
import com.tcs.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService service;

    @PostMapping
    public ResponseEntity<ProjectResponseDto> createProject(@Validated @RequestBody ProjectCreateDto dto) {
        ProjectResponseDto created = service.createProject(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<ProjectResponseDto>> list(
            @RequestParam(required = false) Long managerUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {

        Sort.Order order = new Sort.Order(Sort.Direction.fromString(sort[1]), sort[0]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(order));
        return ResponseEntity.ok(service.listProjects(managerUserId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.getProject(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.softDeleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectId}/resource-requests")
    public ResponseEntity<ResourceRequestResponseDto> createRequest(
            @PathVariable Long projectId,
            @RequestParam Long requesterUserId,
            @RequestBody ResourceRequestCreateDto dto) {

        ResourceRequestResponseDto created = service.createResourceRequest(projectId, requesterUserId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{projectId}/resource-requests")
    public ResponseEntity<Page<ResourceRequestResponseDto>> listByProject(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<ResourceRequestResponseDto> p = service.listResourceRequests(null, pageable)
                .map(r -> r) // identity mapping; we'll filter by projectId in service if needed
                ;
        // simpler: call service.getProject(projectId) and return its requests
        ProjectResponseDto pr = service.getProject(projectId);
        Page<ResourceRequestResponseDto> pageResp = new PageImpl<>(pr.getResourceRequests(), pageable, pr.getResourceRequests().size());
        return ResponseEntity.ok(pageResp);
    }

    @PutMapping("/resource-requests/{id}/close")
    public ResponseEntity<Void> closeRequest(@PathVariable Long id) {
        service.closeRequest(id);
        return ResponseEntity.ok().build();
    }
}
