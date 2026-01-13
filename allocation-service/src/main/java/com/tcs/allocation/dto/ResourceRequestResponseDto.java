package com.tcs.allocation.dto;

import java.time.Instant;
import java.util.List;

import lombok.Data;

@Data
public class ResourceRequestResponseDto {
    private Long id;
    private Long projectId;
    private Long requestedByUserId;
    private Integer totalRequired;
    private Integer minExperienceYears;
    private String priority;
    private String status;
    private Instant createdAt;
    private List<SkillRequirementDto> skills;
}
