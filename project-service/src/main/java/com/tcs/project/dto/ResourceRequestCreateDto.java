package com.tcs.project.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class ResourceRequestCreateDto {
    private Integer totalRequired;
    private Integer minExperienceYears;
    private String priority;
    private List<SkillRequirementDto> skills;
}
