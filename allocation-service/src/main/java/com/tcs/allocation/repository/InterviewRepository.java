package com.tcs.allocation.repository;

import com.tcs.allocation.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByCandidateId(Long candidateId);
}
