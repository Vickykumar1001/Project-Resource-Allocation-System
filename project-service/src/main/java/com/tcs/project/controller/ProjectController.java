package com.tcs.project.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tcs.project.dto.ProjectCreateDto;
import com.tcs.project.dto.ProjectResponseDto;
import com.tcs.project.dto.ResourceRequestCreateDto;
import com.tcs.project.dto.ResourceRequestResponseDto;
import com.tcs.project.service.ProjectService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
public class ProjectController {

	private final ProjectService service;

	@PostMapping
	public ResponseEntity<ProjectResponseDto> createProject(@Validated @RequestBody ProjectCreateDto dto) {
		ProjectResponseDto created = service.createProject(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@GetMapping
	public ResponseEntity<Page<ProjectResponseDto>> list(@RequestParam(required = false) Long managerUserId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
			@RequestParam(defaultValue = "id,asc") String[] sort) {

		Sort.Order order = new Sort.Order(Sort.Direction.fromString(sort[1]), sort[0]);
		Pageable pageable = PageRequest.of(page, size, Sort.by(order));
		return ResponseEntity.ok(service.listProjects(managerUserId, pageable));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProjectResponseDto> get(@PathVariable Long id) {
		return ResponseEntity.ok(service.getProject(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ProjectResponseDto> updateProject(@PathVariable Long id,
			@Validated @RequestBody ProjectCreateDto dto) {
		ProjectResponseDto updated = service.updateProject(id, dto);
		return ResponseEntity.ok(updated);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		service.softDeleteProject(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{projectId}/resource-requests")
	public ResponseEntity<ResourceRequestResponseDto> createRequest(@PathVariable Long projectId,
			@RequestBody ResourceRequestCreateDto dto) {

		ResourceRequestResponseDto created = service.createResourceRequest(projectId, dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@GetMapping("/{projectId}/resource-requests")
	public ResponseEntity<Page<ResourceRequestResponseDto>> listByProject(@PathVariable Long projectId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {

		Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
		ProjectResponseDto pr = service.getProject(projectId);
		Page<ResourceRequestResponseDto> pageResp = new PageImpl<>(pr.getResourceRequests(), pageable,
				pr.getResourceRequests().size());
		return ResponseEntity.ok(pageResp);
	}

	@PutMapping("/resource-requests/{id}/close")
	public ResponseEntity<Void> closeRequest(@PathVariable Long id) {
		service.closeRequest(id);
		return ResponseEntity.ok().build();
	}
}
