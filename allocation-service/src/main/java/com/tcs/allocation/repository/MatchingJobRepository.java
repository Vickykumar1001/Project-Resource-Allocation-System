package com.tcs.allocation.repository;

import com.tcs.allocation.entity.MatchingJob;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchingJobRepository extends JpaRepository<MatchingJob, Long> {
    Page<MatchingJob> findAllByDeletedFalse(Pageable pageable);
    Page<MatchingJob> findByResourceRequestIdAndDeletedFalse(Long resourceRequestId, Pageable pageable);
}
