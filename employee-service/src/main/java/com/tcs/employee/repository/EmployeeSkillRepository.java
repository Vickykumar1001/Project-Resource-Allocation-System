package com.tcs.employee.repository;

import com.tcs.employee.entity.EmployeeSkill;
import com.tcs.employee.entity.EmployeeSkillId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, EmployeeSkillId> {
}
