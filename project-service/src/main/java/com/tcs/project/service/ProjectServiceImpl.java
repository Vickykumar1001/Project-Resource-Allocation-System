package com.tcs.project.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tcs.project.client.AuthClient;
import com.tcs.project.dto.AllocationDto;
import com.tcs.project.dto.CandidateSuggestionDto;
import com.tcs.project.dto.ProjectCreateDto;
import com.tcs.project.dto.ProjectResponseDto;
import com.tcs.project.dto.ResourceRequestCreateDto;
import com.tcs.project.dto.ResourceRequestResponseDto;
import com.tcs.project.dto.UserDto;
import com.tcs.project.entity.AllocationRecord;
import com.tcs.project.entity.CandidateSuggestion;
import com.tcs.project.entity.Project;
import com.tcs.project.entity.ResourceRequest;
import com.tcs.project.entity.ResourceSkillRequirement;
import com.tcs.project.exception.AuthServiceException;
import com.tcs.project.exception.NotFoundException;
import com.tcs.project.mapper.ProjectMapper;
import com.tcs.project.repository.AllocationRepository;
import com.tcs.project.repository.CandidateSuggestionRepository;
import com.tcs.project.repository.ProjectRepository;
import com.tcs.project.repository.ResourceRequestRepository;
import com.tcs.project.repository.ResourceSkillRequirementRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

	private final ProjectRepository projectRepo;
	private final ResourceRequestRepository requestRepo;
	private final ResourceSkillRequirementRepository skillReqRepo;
	private final CandidateSuggestionRepository suggestionRepo;
	private final AllocationRepository allocationRepo;
	private final AuthClient authClient;

	@Transactional
	@Override
	public ProjectResponseDto createProject(ProjectCreateDto dto) {
		UserDto usr = fetchUserDtoByToken();
		Project p = Project.builder().projectCode(dto.getProjectCode()).projectName(dto.getProjectName())
				.description(dto.getDescription()).managerUserId(usr.getId()).startDate(dto.getStartDate())
				.endDate(dto.getEndDate()).status(Project.Status.PLANNED).deleted(false).build();

		p = projectRepo.save(p);
		return ProjectMapper.toProjectDto(p);
	}
 
	@Transactional
	@Override
	public ProjectResponseDto updateProject(Long id, ProjectCreateDto dto) {
		Project p = projectRepo.findByIdAndDeletedFalse(id)
				.orElseThrow(() -> new NotFoundException("Project not found: " + id));

		// update allowed fields
		if (dto.getProjectCode() != null)
			p.setProjectCode(dto.getProjectCode());
		if (dto.getProjectName() != null)
			p.setProjectName(dto.getProjectName());
		if (dto.getDescription() != null)
			p.setDescription(dto.getDescription());
		if (dto.getStartDate() != null)
			p.setStartDate(dto.getStartDate());
		if (dto.getEndDate() != null)
			p.setEndDate(dto.getEndDate());
		// status update allowed if provided
		if (dto.getStatus() != null)
			p.setStatus(Project.Status.valueOf(dto.getStatus()));

		projectRepo.save(p);
		return ProjectMapper.toProjectDto(p);
	}

	@Override
	public Page<ProjectResponseDto> listProjects(Long managerUserId, Pageable pageable) {
		Page<Project> page = (managerUserId != null)
				? projectRepo.findByManagerUserIdAndDeletedFalse(managerUserId, pageable)
				: projectRepo.findAllByDeletedFalse(pageable);
		return page.map(ProjectMapper::toProjectDto);
	}

	@Override
	public ProjectResponseDto getProject(Long id) {
		Project p = projectRepo.findByIdAndDeletedFalse(id)
				.orElseThrow(() -> new NotFoundException("Project not found: " + id));
		return ProjectMapper.toProjectDto(p);
	}

	@Transactional
	@Override
	public void softDeleteProject(Long id) {
		Project p = projectRepo.findByIdAndDeletedFalse(id)
				.orElseThrow(() -> new NotFoundException("Project not found: " + id));
		p.setDeleted(true);
		projectRepo.save(p);
	}

	@Transactional
	@Override
	public ResourceRequestResponseDto createResourceRequest(Long projectId, ResourceRequestCreateDto dto) {
		Project p = projectRepo.findByIdAndDeletedFalse(projectId)
				.orElseThrow(() -> new NotFoundException("Project not found: " + projectId));
		UserDto usr = fetchUserDtoByToken();
		ResourceRequest req = new ResourceRequest();
		req.setProject(p);
		req.setRequestedByUserId(usr.getId());
		req.setTotalRequired(dto.getTotalRequired());
		req.setMinExperienceYears(dto.getMinExperienceYears());
		req.setPriority(dto.getPriority() != null ? ResourceRequest.Priority.valueOf(dto.getPriority())
				: ResourceRequest.Priority.LOW);
		req.setStatus(ResourceRequest.RequestStatus.OPEN);
		req.setDeleted(false);
		ResourceRequest reqNew = requestRepo.save(req);

		if (dto.getSkills() != null) {
			List<ResourceSkillRequirement> srs = dto.getSkills().stream().map(s -> {
				ResourceSkillRequirement r = new ResourceSkillRequirement();
				r.setSkillName(s.getSkillName());
				r.setRequiredCount(s.getRequiredCount());
				r.setMinProficiency(s.getMinProficiency() != null
						? ResourceSkillRequirement.MinProficiency.valueOf(s.getMinProficiency())
						: null);
				r.setResourceRequest(reqNew);
				return r;
			}).collect(Collectors.toList());
			skillReqRepo.saveAll(srs);
			reqNew.setSkillRequirements(srs);
		}

		p.getResourceRequests().add(reqNew);
		projectRepo.save(p);

		return ProjectMapper.toRequestDto(reqNew);
	}

	@Override
	public Page<ResourceRequestResponseDto> listResourceRequests(ResourceRequest.RequestStatus status,
			Pageable pageable) {
		Page<ResourceRequest> page = (status != null) ? requestRepo.findByStatusAndDeletedFalse(status, pageable)
				: requestRepo.findAllByDeletedFalse(pageable);
		return page.map(ProjectMapper::toRequestDto);
	}

	@Override
	public ResourceRequestResponseDto getRequest(Long id) {
		ResourceRequest r = requestRepo.findByIdAndDeletedFalse(id)
				.orElseThrow(() -> new NotFoundException("Request not found: " + id));
		return ProjectMapper.toRequestDto(r);
	}

	@Transactional
	@Override
	public void closeRequest(Long id) {
		ResourceRequest r = requestRepo.findByIdAndDeletedFalse(id)
				.orElseThrow(() -> new NotFoundException("Request not found: " + id));
		r.setStatus(ResourceRequest.RequestStatus.CLOSED);
		requestRepo.save(r);
	}

	@Transactional
	@Override
	public CandidateSuggestionDto suggestCandidate(Long requestId, CandidateSuggestionDto dto) {
		ResourceRequest req = requestRepo.findByIdAndDeletedFalse(requestId)
				.orElseThrow(() -> new NotFoundException("Request not found: " + requestId));

		CandidateSuggestion s = CandidateSuggestion.builder().resourceRequest(req).employeeId(dto.getEmployeeId())
				.suggestedByUserId(dto.getSuggestedByUserId()).matchScore(dto.getMatchScore())
				.status(CandidateSuggestion.SuggestionStatus.SUGGESTED).build();

		s = suggestionRepo.save(s);
		if (req.getStatus() == ResourceRequest.RequestStatus.OPEN) {
			req.setStatus(ResourceRequest.RequestStatus.IN_PROGRESS);
			requestRepo.save(req);
		}
		return ProjectMapper.toSuggestionDto(s);
	}

	@Override
	public List<CandidateSuggestionDto> listSuggestions(Long requestId) {
		List<CandidateSuggestion> list = suggestionRepo.findByResourceRequestId(requestId);
		return list.stream().map(ProjectMapper::toSuggestionDto).collect(Collectors.toList());
	}

	@Transactional
	@Override
	public CandidateSuggestionDto reviewSuggestion(Long suggestionId, boolean approve, String feedback) {
		CandidateSuggestion s = suggestionRepo.findById(suggestionId)
				.orElseThrow(() -> new NotFoundException("Suggestion not found: " + suggestionId));
		s.setManagerFeedback(feedback);
		s.setStatus(approve ? CandidateSuggestion.SuggestionStatus.APPROVED
				: CandidateSuggestion.SuggestionStatus.REJECTED);
		s = suggestionRepo.save(s);
		return ProjectMapper.toSuggestionDto(s);
	}

	@Transactional
	@Override
	public AllocationDto allocateEmployee(Long requestId, Long employeeId, Long allocatedByUserId) {
		ResourceRequest req = requestRepo.findByIdAndDeletedFalse(requestId)
				.orElseThrow(() -> new NotFoundException("Request not found: " + requestId));

		boolean approved = suggestionRepo.findByResourceRequestId(requestId).stream()
				.anyMatch(s -> s.getEmployeeId().equals(employeeId)
						&& s.getStatus() == CandidateSuggestion.SuggestionStatus.APPROVED);

		if (!approved) {
			throw new IllegalArgumentException("Employee must be approved by manager before allocation");
		}

		AllocationRecord a = AllocationRecord.builder().employeeId(employeeId).projectId(req.getProject().getId())
				.resourceRequestId(requestId).allocatedByUserId(allocatedByUserId)
				.status(AllocationRecord.AllocationStatus.ALLOCATED).build();

		a = allocationRepo.save(a);

		long allocatedCount = allocationRepo.findByResourceRequestId(requestId).stream()
				.filter(ar -> ar.getStatus() == AllocationRecord.AllocationStatus.ALLOCATED).count();
		if (req.getTotalRequired() != null && allocatedCount >= req.getTotalRequired()) {
			req.setStatus(ResourceRequest.RequestStatus.FULFILLED);
		} else {
			req.setStatus(ResourceRequest.RequestStatus.PARTIALLY_FULFILLED);
		}
		requestRepo.save(req);

		return ProjectMapper.toAllocationDto(a);
	}

	@Override
	public List<AllocationDto> listAllocationsByRequest(Long requestId) {
		return allocationRepo.findByResourceRequestId(requestId).stream().map(ProjectMapper::toAllocationDto)
				.collect(Collectors.toList());
	}

	@Transactional
	@Override
	public void releaseAllocation(Long allocationId) {
		AllocationRecord a = allocationRepo.findById(allocationId)
				.orElseThrow(() -> new NotFoundException("Allocation not found: " + allocationId));
		a.setStatus(AllocationRecord.AllocationStatus.RELEASED);
		allocationRepo.save(a);

		Long requestId = a.getResourceRequestId();
		if (requestId != null) {
			ResourceRequest req = requestRepo.findById(requestId).orElse(null);
			if (req != null) {
				req.setStatus(ResourceRequest.RequestStatus.PARTIALLY_FULFILLED);
				requestRepo.save(req);
			}
		}
	}

	private UserDto fetchUserDtoByToken() {
		try {
			ResponseEntity<UserDto> usr = authClient.getUserFromToken();
			if (usr == null || usr.getBody() == null) {
				throw new AuthServiceException("Unable to fetch user from auth service");
			}
			return usr.getBody();
		} catch (Exception ex) {
			throw new AuthServiceException("Auth service error: " + ex.getMessage());
		}
	}

	@Override
	public AllocationDto getAllocation(Long allocationId) {
		AllocationRecord a = allocationRepo.findById(allocationId)
				.orElseThrow(() -> new NotFoundException("Allocation not found: " + allocationId));
		return ProjectMapper.toAllocationDto(a);
	}
}
