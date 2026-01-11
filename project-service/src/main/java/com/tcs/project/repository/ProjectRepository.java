package com.tcs.project.repository;

import com.tcs.project.entity.Project;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findAllByDeletedFalse(Pageable pageable);
    Page<Project> findByManagerUserIdAndDeletedFalse(Long managerUserId, Pageable pageable);
}
