package com.tcs.employee.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCreateRequest {
    private Long userId;
    private Integer experienceYears;
    private String location;
    private List<SkillRequest> skills;
}
