package com.tcs.employee.dto;

import com.tcs.employee.entity.EmployeeSkill.Proficiency;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillRequest {
    private String name;
    private Proficiency proficiency;
}
