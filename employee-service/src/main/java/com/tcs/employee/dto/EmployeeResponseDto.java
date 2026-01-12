package com.tcs.employee.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponseDto {
    private Long id;
    private Long userId;     // from auth
    private String fullName; // from auth
    private String email;    // from auth
    private String role;     // from auth
    private Integer experienceYears;
    private String currentStatus;
    private Long currentProjectId;
    private String location;
    private List<SkillRequest> skills;
}
