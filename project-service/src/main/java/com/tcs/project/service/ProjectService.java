package com.tcs.project.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tcs.project.dto.AllocationDto;
import com.tcs.project.dto.CandidateSuggestionDto;
import com.tcs.project.dto.ProjectCreateDto;
import com.tcs.project.dto.ProjectResponseDto;
import com.tcs.project.dto.ResourceRequestCreateDto;
import com.tcs.project.dto.ResourceRequestResponseDto;
import com.tcs.project.entity.ResourceRequest;

public interface ProjectService {

	ProjectResponseDto createProject(ProjectCreateDto dto);

	ProjectResponseDto updateProject(Long id, ProjectCreateDto dto);

	Page<ProjectResponseDto> listProjects(Long managerUserId, Pageable pageable);

	ProjectResponseDto getProject(Long id);

	void softDeleteProject(Long id);

	ResourceRequestResponseDto createResourceRequest(Long projectId, ResourceRequestCreateDto dto);

	Page<ResourceRequestResponseDto> listResourceRequests(ResourceRequest.RequestStatus status, Pageable pageable);

	ResourceRequestResponseDto getRequest(Long id);

	void closeRequest(Long id);

	CandidateSuggestionDto suggestCandidate(Long requestId, CandidateSuggestionDto dto);

	List<CandidateSuggestionDto> listSuggestions(Long requestId);

	CandidateSuggestionDto reviewSuggestion(Long suggestionId, boolean approve, String feedback);

	AllocationDto allocateEmployee(Long requestId, Long employeeId, Long allocatedByUserId);

	AllocationDto getAllocation(Long allocationId);

	List<AllocationDto> listAllocationsByRequest(Long requestId);

	void releaseAllocation(Long allocationId);
}
