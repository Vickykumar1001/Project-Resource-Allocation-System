package com.tcs.employee.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tcs.employee.entity.EmployeeProfile;

public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, Long> {

    Optional<EmployeeProfile> findByUserIdAndDeletedFalse(Long userId);

    Optional<EmployeeProfile> findByIdAndDeletedFalse(Long id);

    Page<EmployeeProfile> findAllByDeletedFalse(Pageable pageable);

    List<EmployeeProfile> findAllByIdInAndDeletedFalse(List<Long> ids);

    // fetch by skill name (joins through EmployeeSkill)
    Page<EmployeeProfile> findBySkills_Skill_NameIgnoreCaseAndDeletedFalse(String skillName, Pageable pageable);
}
