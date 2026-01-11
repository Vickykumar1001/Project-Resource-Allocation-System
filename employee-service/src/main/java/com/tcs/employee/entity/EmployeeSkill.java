package com.tcs.employee.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employee_skill")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSkill {

    @EmbeddedId
    private EmployeeSkillId id = new EmployeeSkillId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("employeeId")
    private EmployeeProfile employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("skillId")
    private Skill skill;

    @Enumerated(EnumType.STRING)
    private Proficiency proficiency;

    public static enum Proficiency {
        BEGINNER,
        INTERMEDIATE,
        EXPERT
    }
}
