package com.tcs.employee.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tcs.employee.entity.EmployeeSkill;
import com.tcs.employee.entity.EmployeeSkillId;

public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, EmployeeSkillId> {

    List<EmployeeSkill> findByEmployee_Id(Long employeeId);
}
