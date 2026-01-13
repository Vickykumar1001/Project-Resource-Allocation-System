package com.tcs.allocation.dto;

import lombok.Data;

@Data
public class SkillRequirementDto {
    private String skillName;
    private Integer requiredCount;
    private String minProficiency;
}
