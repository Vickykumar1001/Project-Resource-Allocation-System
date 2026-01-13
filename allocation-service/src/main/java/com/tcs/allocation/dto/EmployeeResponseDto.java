package com.tcs.allocation.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
