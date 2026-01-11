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
    private Long userId;
    private String fullName; // from Auth
    private String email;    // from Auth
    private String role;     // from Auth
    private Integer experienceYears;
    private String currentStatus;
    private Long currentProjectId;
    private String location;
    private List<SkillRequest> skills;
}
