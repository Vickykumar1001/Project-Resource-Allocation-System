package com.tcs.employee.repository;

import com.tcs.employee.entity.EmployeeProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, Long> {

    Optional<EmployeeProfile> findByUserIdAndDeletedFalse(Long userId);

    Page<EmployeeProfile> findAllByDeletedFalse(Pageable pageable);

    List<EmployeeProfile> findAllByIdInAndDeletedFalse(List<Long> ids);

    // filter by skill name (join)
    @Query(value = "select distinct ep from EmployeeProfile ep join ep.skills es join es.skill s " +
            "where ep.deleted = false and lower(s.name) = lower(:skillName)",
            countQuery = "select count(distinct ep) from EmployeeProfile ep join ep.skills es join es.skill s " +
                    "where ep.deleted = false and lower(s.name) = lower(:skillName)")
    Page<EmployeeProfile> findBySkillName(@Param("skillName") String skillName, Pageable pageable);
}
