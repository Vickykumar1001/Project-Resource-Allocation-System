package com.tcs.project.dto;

import lombok.Data;
import java.time.Instant;
import java.util.List;

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
