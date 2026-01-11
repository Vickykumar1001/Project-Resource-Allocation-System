package com.tcs.employee.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeUpdateRequest {
    private Integer experienceYears;
    private String location;
    private List<SkillRequest> skills;
}
