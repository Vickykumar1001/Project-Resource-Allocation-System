package com.tcs.employee.mapper;

import java.util.stream.Collectors;

import com.tcs.employee.dto.EmployeeCreateRequest;
import com.tcs.employee.dto.EmployeeResponseDto;
import com.tcs.employee.dto.SkillRequest;
import com.tcs.employee.dto.UserDto;
import com.tcs.employee.entity.EmployeeProfile;

public class EmployeeMapper {

    public static EmployeeResponseDto toResponseDto(EmployeeProfile p, UserDto userDto) {
        EmployeeResponseDto dto = new EmployeeResponseDto();
        dto.setId(p.getId());
        dto.setUserId(p.getUserId());
        if (userDto != null) {
            dto.setFullName(userDto.getFullName());
            dto.setEmail(userDto.getEmail());
            dto.setRole(userDto.getRole());
        }
        dto.setExperienceYears(p.getExperienceYears());
        dto.setCurrentStatus(p.getCurrentStatus() != null ? p.getCurrentStatus().name() : null);
        dto.setCurrentProjectId(p.getCurrentProjectId());
        dto.setLocation(p.getLocation());
        if (p.getSkills() != null) {
            dto.setSkills(p.getSkills().stream().map(es ->
                    new SkillRequest(es.getSkill().getName(), es.getProficiency())).collect(Collectors.toList()));
        }
        return dto;
    }

    public static EmployeeProfile fromCreateRequest(EmployeeCreateRequest req) {
        EmployeeProfile p = new EmployeeProfile();
        p.setUserId(req.getUserId());
        p.setExperienceYears(req.getExperienceYears());
        p.setLocation(req.getLocation());
        p.setCurrentStatus(EmployeeProfile.CurrentStatus.AVAILABLE);
        p.setDeleted(false);
        return p;
    }
}
