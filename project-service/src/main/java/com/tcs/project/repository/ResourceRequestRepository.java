package com.tcs.project.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tcs.project.entity.ResourceRequest;
import com.tcs.project.entity.ResourceRequest.RequestStatus;

public interface ResourceRequestRepository extends JpaRepository<ResourceRequest, Long> {
	Optional<ResourceRequest> findByIdAndDeletedFalse(Long id);

	Page<ResourceRequest> findAllByDeletedFalse(Pageable pageable);

	Page<ResourceRequest> findByStatusAndDeletedFalse(RequestStatus status, Pageable pageable);

	Page<ResourceRequest> findByProjectIdAndDeletedFalse(Long projectId, Pageable pageable);
}
