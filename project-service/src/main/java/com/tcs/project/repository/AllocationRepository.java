package com.tcs.project.repository;

import com.tcs.project.entity.AllocationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllocationRepository extends JpaRepository<AllocationRecord, Long> {
    List<AllocationRecord> findByProjectId(Long projectId);
    List<AllocationRecord> findByResourceRequestId(Long requestId);
}
