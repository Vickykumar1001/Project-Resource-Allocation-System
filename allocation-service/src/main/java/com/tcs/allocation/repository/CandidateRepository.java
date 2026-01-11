package com.tcs.allocation.repository;

import com.tcs.allocation.entity.Candidate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    Page<Candidate> findByMatchingJobIdAndDeletedFalse(Long matchingJobId, Pageable pageable);
    List<Candidate> findByResourceRequestIdAndDeletedFalse(Long requestId);
}
