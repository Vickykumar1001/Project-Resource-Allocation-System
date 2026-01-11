package com.tcs.allocation.service;

import com.tcs.allocation.entity.*;
import com.tcs.allocation.repository.*;
import com.tcs.allocation.dto.*;
import com.tcs.allocation.mapper.AllocationMapper;
import com.tcs.allocation.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CandidateService {

    private final CandidateRepository candidateRepo;
    private final InterviewRepository interviewRepo;
    private final AllocationAttemptRepository allocationRepo;

    public List<CandidateDto> listByRequest(Long requestId) {
        return candidateRepo.findByResourceRequestIdAndDeletedFalse(requestId).stream().map(AllocationMapper::toCandidateDto).collect(Collectors.toList());
    }

    public CandidateDto getCandidate(Long id) {
        Candidate c = candidateRepo.findById(id).orElseThrow(() -> new NotFoundException("Candidate not found: " + id));
        return AllocationMapper.toCandidateDto(c);
    }

    @Transactional
    public InterviewDto scheduleInterview(Long candidateId, InterviewDto dto) {
        Candidate c = candidateRepo.findById(candidateId).orElseThrow(() -> new NotFoundException("Candidate not found: " + candidateId));
        Interview i = Interview.builder()
                .candidateId(candidateId)
                .scheduledByUserId(dto.getScheduledByUserId())
                .scheduledAt(dto.getScheduledAt())
                .mode(dto.getMode())
                .interviewerUserId(dto.getInterviewerUserId())
                .result(Interview.Result.PENDING)
                .build();
        i = interviewRepo.save(i);
        c.setStatus(Candidate.Status.INTERVIEW_SCHEDULED);
        candidateRepo.save(c);
        return AllocationMapper.toInterviewDto(i);
    }

    @Transactional
    public InterviewDto recordInterviewResult(Long interviewId, InterviewDto dto) {
        Interview i = interviewRepo.findById(interviewId).orElseThrow(() -> new NotFoundException("Interview not found: " + interviewId));
        i.setResult(Interview.Result.valueOf(dto.getResult()));
        i.setFeedback(dto.getFeedback());
        i = interviewRepo.save(i);
        // update candidate status
        Candidate c = candidateRepo.findById(i.getCandidateId()).orElse(null);
        if (c != null) {
            if (i.getResult() == Interview.Result.PASSED) c.setStatus(Candidate.Status.INTERVIEW_PASSED);
            else if (i.getResult() == Interview.Result.FAILED) c.setStatus(Candidate.Status.INTERVIEW_FAILED);
            candidateRepo.save(c);
        }
        return AllocationMapper.toInterviewDto(i);
    }

    public List<InterviewDto> listInterviews(Long candidateId) {
        return interviewRepo.findByCandidateId(candidateId).stream().map(AllocationMapper::toInterviewDto).collect(Collectors.toList());
    }
}
