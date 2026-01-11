package com.tcs.project.repository;

import com.tcs.project.entity.CandidateSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidateSuggestionRepository extends JpaRepository<CandidateSuggestion, Long> {
    List<CandidateSuggestion> findByResourceRequestId(Long resourceRequestId);
}
