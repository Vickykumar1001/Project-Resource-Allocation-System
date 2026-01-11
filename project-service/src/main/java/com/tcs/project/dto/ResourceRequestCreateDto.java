package com.tcs.project.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class ResourceRequestCreateDto {
    @NotNull
    private Integer totalRequired;
    private Integer minExperienceYears;
    private String priority; // LOW/MEDIUM/HIGH
    private List<SkillRequirementDto> skills;
}
