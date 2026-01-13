package com.tcs.project.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tcs.project.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
	Optional<Project> findByIdAndDeletedFalse(Long id);
	Page<Project> findAllByDeletedFalse(Pageable pageable);
    Page<Project> findByManagerUserIdAndDeletedFalse(Long managerUserId, Pageable pageable);
}
