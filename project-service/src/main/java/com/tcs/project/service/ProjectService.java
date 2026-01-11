package com.tcs.project.service;

import com.tcs.project.dto.*;
import com.tcs.project.entity.*;
import com.tcs.project.exception.NotFoundException;
import com.tcs.project.mapper.ProjectMapper;
import com.tcs.project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepo;
    private final ResourceRequestRepository requestRepo;
    private final ResourceSkillRequirementRepository skillReqRepo;
    private final CandidateSuggestionRepository suggestionRepo;
    private final AllocationRepository allocationRepo;

    // PROJECT CRUD
    @Transactional
    public ProjectResponseDto createProject(ProjectCreateDto dto) {
        Project p = Project.builder()
                .projectCode(dto.getProjectCode())
                .projectName(dto.getProjectName())
                .description(dto.getDescription())
                .managerUserId(dto.getManagerUserId())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(Project.Status.PLANNED)
                .deleted(false)
                .build();

        p = projectRepo.save(p);
        return ProjectMapper.toProjectDto(p);
    }

    public Page<ProjectResponseDto> listProjects(Long managerUserId, Pageable pageable) {
        Page<Project> page = (managerUserId != null) ?
                projectRepo.findByManagerUserIdAndDeletedFalse(managerUserId, pageable) :
                projectRepo.findAllByDeletedFalse(pageable);
        return page.map(ProjectMapper::toProjectDto);
    }

    public ProjectResponseDto getProject(Long id) {
        Project p = projectRepo.findById(id).filter(x -> !x.isDeleted())
                .orElseThrow(() -> new NotFoundException("Project not found: " + id));
        return ProjectMapper.toProjectDto(p);
    }

    @Transactional
    public void softDeleteProject(Long id) {
        Project p = projectRepo.findById(id).orElseThrow(() -> new NotFoundException("Project not found: " + id));
        p.setDeleted(true);
        projectRepo.save(p);
    }

    // RESOURCE REQUESTS
    @Transactional
    public ResourceRequestResponseDto createResourceRequest(Long projectId, Long requesterUserId, ResourceRequestCreateDto dto) {
        Project p = projectRepo.findById(projectId)
                .filter(x -> !x.isDeleted())
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        ResourceRequest req = new ResourceRequest();
        req.setProject(p);
        req.setRequestedByUserId(requesterUserId);
        req.setTotalRequired(dto.getTotalRequired());
        req.setMinExperienceYears(dto.getMinExperienceYears());
        req.setPriority(dto.getPriority() != null ? ResourceRequest.Priority.valueOf(dto.getPriority()) : ResourceRequest.Priority.MEDIUM);
        req.setStatus(ResourceRequest.RequestStatus.OPEN);
        req.setDeleted(false);
        ResourceRequest reqNew = requestRepo.save(req);

        // persist skill requirements
        if (dto.getSkills() != null) {
            List<ResourceSkillRequirement> srs = dto.getSkills().stream().map(s -> {
                ResourceSkillRequirement r = new ResourceSkillRequirement();
                r.setSkillName(s.getSkillName());
                r.setRequiredCount(s.getRequiredCount());
                r.setMinProficiency(s.getMinProficiency() != null ? ResourceSkillRequirement.MinProficiency.valueOf(s.getMinProficiency()) : null);
                r.setResourceRequest(reqNew);
                return r;
            }).collect(Collectors.toList());
            skillReqRepo.saveAll(srs);
            reqNew.setSkillRequirements(srs);
        }

        // update project relationship
        p.getResourceRequests().add(reqNew);
        projectRepo.save(p);

        return ProjectMapper.toRequestDto(req);
    }

    public Page<ResourceRequestResponseDto> listResourceRequests(ResourceRequest.RequestStatus status, Pageable pageable) {
        Page<ResourceRequest> page = (status != null) ?
                requestRepo.findByStatusAndDeletedFalse(status, pageable) :
                requestRepo.findAllByDeletedFalse(pageable);
        return page.map(ProjectMapper::toRequestDto);
    }

    public ResourceRequestResponseDto getRequest(Long id) {
        ResourceRequest r = requestRepo.findById(id).filter(x -> !x.isDeleted())
                .orElseThrow(() -> new NotFoundException("Request not found: " + id));
        return ProjectMapper.toRequestDto(r);
    }

    @Transactional
    public void closeRequest(Long id) {
        ResourceRequest r = requestRepo.findById(id).filter(x -> !x.isDeleted())
                .orElseThrow(() -> new NotFoundException("Request not found: " + id));
        r.setStatus(ResourceRequest.RequestStatus.CLOSED);
        requestRepo.save(r);
    }

    // SUGGESTIONS (RMG suggests candidates)
    @Transactional
    public CandidateSuggestionDto suggestCandidate(Long requestId, CandidateSuggestionDto dto) {
        ResourceRequest req = requestRepo.findById(requestId).filter(x -> !x.isDeleted())
                .orElseThrow(() -> new NotFoundException("Request not found: " + requestId));

        CandidateSuggestion s = CandidateSuggestion.builder()
                .resourceRequest(req)
                .employeeId(dto.getEmployeeId())
                .suggestedByUserId(dto.getSuggestedByUserId())
                .matchScore(dto.getMatchScore())
                .status(CandidateSuggestion.SuggestionStatus.SUGGESTED)
                .build();

        s = suggestionRepo.save(s);
        // update request status to IN_PROGRESS if open
        if (req.getStatus() == ResourceRequest.RequestStatus.OPEN) {
            req.setStatus(ResourceRequest.RequestStatus.IN_PROGRESS);
            requestRepo.save(req);
        }
        return ProjectMapper.toSuggestionDto(s);
    }

    public List<CandidateSuggestionDto> listSuggestions(Long requestId) {
        List<CandidateSuggestion> list = suggestionRepo.findByResourceRequestId(requestId);
        return list.stream().map(ProjectMapper::toSuggestionDto).collect(Collectors.toList());
    }

    // Manager approves/rejects suggestion
    @Transactional
    public CandidateSuggestionDto reviewSuggestion(Long suggestionId, boolean approve, String feedback) {
        CandidateSuggestion s = suggestionRepo.findById(suggestionId)
                .orElseThrow(() -> new NotFoundException("Suggestion not found: " + suggestionId));
        s.setManagerFeedback(feedback);
        if (approve) s.setStatus(CandidateSuggestion.SuggestionStatus.APPROVED);
        else s.setStatus(CandidateSuggestion.SuggestionStatus.REJECTED);
        s = suggestionRepo.save(s);
        return ProjectMapper.toSuggestionDto(s);
    }

    // Allocation recording by RMG after manager approval
    @Transactional
    public AllocationDto allocateEmployee(Long requestId, Long employeeId, Long allocatedByUserId) {
        ResourceRequest req = requestRepo.findById(requestId).filter(x -> !x.isDeleted())
                .orElseThrow(() -> new NotFoundException("Request not found: " + requestId));

        // require that there is an approved suggestion for this employee
        boolean approved = suggestionRepo.findByResourceRequestId(requestId).stream()
                .anyMatch(s -> s.getEmployeeId().equals(employeeId) && s.getStatus() == CandidateSuggestion.SuggestionStatus.APPROVED);

        if (!approved) {
            throw new IllegalArgumentException("Employee must be approved by manager before allocation");
        }

        AllocationRecord a = AllocationRecord.builder()
                .employeeId(employeeId)
                .projectId(req.getProject().getId())
                .resourceRequestId(requestId)
                .allocatedByUserId(allocatedByUserId)
                .status(AllocationRecord.AllocationStatus.ALLOCATED)
                .build();

        a = allocationRepo.save(a);

        // after allocation, you may update request status and counts
        // naive approach: if number of allocations >= totalRequired => mark FULFILLED
        long allocatedCount = allocationRepo.findByResourceRequestId(requestId).stream()
                .filter(ar -> ar.getStatus() == AllocationRecord.AllocationStatus.ALLOCATED)
                .count();
        if (req.getTotalRequired() != null && allocatedCount >= req.getTotalRequired()) {
            req.setStatus(ResourceRequest.RequestStatus.FULFILLED);
        } else {
            req.setStatus(ResourceRequest.RequestStatus.PARTIALLY_FULFILLED);
        }
        requestRepo.save(req);

        return ProjectMapper.toAllocationDto(a);
    }

    public List<AllocationDto> listAllocationsByRequest(Long requestId) {
        return allocationRepo.findByResourceRequestId(requestId).stream().map(ProjectMapper::toAllocationDto).collect(Collectors.toList());
    }

    // Release allocation (if RMG wants to release)
    @Transactional
    public void releaseAllocation(Long allocationId) {
        AllocationRecord a = allocationRepo.findById(allocationId).orElseThrow(() -> new NotFoundException("Allocation not found: " + allocationId));
        a.setStatus(AllocationRecord.AllocationStatus.RELEASED);
        allocationRepo.save(a);

        // update request status potentially
        Long requestId = a.getResourceRequestId();
        if (requestId != null) {
            ResourceRequest req = requestRepo.findById(requestId).orElse(null);
            if (req != null) {
                req.setStatus(ResourceRequest.RequestStatus.IN_PROGRESS);
                requestRepo.save(req);
            }
        }
    }
}
