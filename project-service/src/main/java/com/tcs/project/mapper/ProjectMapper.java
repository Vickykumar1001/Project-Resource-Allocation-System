package com.tcs.project.mapper;


import java.util.stream.Collectors;

import com.tcs.project.dto.AllocationDto;
import com.tcs.project.dto.CandidateSuggestionDto;
import com.tcs.project.dto.ProjectResponseDto;
import com.tcs.project.dto.ResourceRequestResponseDto;
import com.tcs.project.dto.SkillRequirementDto;
import com.tcs.project.entity.AllocationRecord;
import com.tcs.project.entity.CandidateSuggestion;
import com.tcs.project.entity.Project;
import com.tcs.project.entity.ResourceRequest;

public class ProjectMapper {

    public static ProjectResponseDto toProjectDto(Project p) {
        ProjectResponseDto dto = new ProjectResponseDto();
        dto.setId(p.getId());
        dto.setProjectCode(p.getProjectCode());
        dto.setProjectName(p.getProjectName());
        dto.setDescription(p.getDescription());
        dto.setManagerUserId(p.getManagerUserId());
        dto.setStartDate(p.getStartDate());
        dto.setEndDate(p.getEndDate());
        dto.setStatus(p.getStatus() != null ? p.getStatus().name() : null);
        if (p.getResourceRequests() != null) {
            dto.setResourceRequests(p.getResourceRequests().stream().map(ProjectMapper::toRequestDto).collect(Collectors.toList()));
        }
        return dto;
    }

    public static ResourceRequestResponseDto toRequestDto(ResourceRequest r) {
        ResourceRequestResponseDto dto = new ResourceRequestResponseDto();
        dto.setId(r.getId());
        dto.setProjectId(r.getProject() != null ? r.getProject().getId() : null);
        dto.setRequestedByUserId(r.getRequestedByUserId());
        dto.setTotalRequired(r.getTotalRequired());
        dto.setMinExperienceYears(r.getMinExperienceYears());
        dto.setPriority(r.getPriority() != null ? r.getPriority().name() : null);
        dto.setStatus(r.getStatus() != null ? r.getStatus().name() : null);
        dto.setCreatedAt(r.getCreatedAt());
        if (r.getSkillRequirements() != null) {
            dto.setSkills(r.getSkillRequirements().stream().map(s -> {
                SkillRequirementDto sd = new SkillRequirementDto();
                sd.setSkillName(s.getSkillName());
                sd.setRequiredCount(s.getRequiredCount());
                sd.setMinProficiency(s.getMinProficiency() != null ? s.getMinProficiency().name() : null);
                return sd;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    public static CandidateSuggestionDto toSuggestionDto(CandidateSuggestion s) {
        CandidateSuggestionDto dto = new CandidateSuggestionDto();
        dto.setId(s.getId());
        dto.setResourceRequestId(s.getResourceRequest() != null ? s.getResourceRequest().getId() : null);
        dto.setEmployeeId(s.getEmployeeId());
        dto.setSuggestedByUserId(s.getSuggestedByUserId());
        dto.setMatchScore(s.getMatchScore());
        dto.setStatus(s.getStatus() != null ? s.getStatus().name() : null);
        dto.setManagerFeedback(s.getManagerFeedback());
        return dto;
    }

    public static AllocationDto toAllocationDto(AllocationRecord a) {
        AllocationDto dto = new AllocationDto();
        dto.setId(a.getId());
        dto.setEmployeeId(a.getEmployeeId());
        dto.setProjectId(a.getProjectId());
        dto.setResourceRequestId(a.getResourceRequestId());
        dto.setAllocatedByUserId(a.getAllocatedByUserId());
        dto.setAllocatedAt(a.getAllocatedAt());
        dto.setStatus(a.getStatus() != null ? a.getStatus().name() : null);
        return dto;
    }
}
