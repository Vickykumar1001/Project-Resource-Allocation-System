package com.tcs.project.dto;

import lombok.Data;

@Data
public class SkillRequirementDto {
    private String skillName;
    private Integer requiredCount;
    private String minProficiency;
}
