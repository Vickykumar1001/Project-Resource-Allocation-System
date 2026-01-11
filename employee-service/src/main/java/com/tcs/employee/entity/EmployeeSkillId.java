package com.tcs.employee.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSkillId implements Serializable {
    private Long employeeId;
    private Long skillId;
}
