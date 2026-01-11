package com.tcs.project.repository;

import com.tcs.project.entity.ResourceRequest;
import com.tcs.project.entity.ResourceRequest.RequestStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRequestRepository extends JpaRepository<ResourceRequest, Long> {
    Page<ResourceRequest> findAllByDeletedFalse(Pageable pageable);
    Page<ResourceRequest> findByStatusAndDeletedFalse(RequestStatus status, Pageable pageable);
    Page<ResourceRequest> findByProjectIdAndDeletedFalse(Long projectId, Pageable pageable);
}
