package com.tcs.allocation.mapper;

import com.tcs.allocation.dto.*;
import com.tcs.allocation.entity.*;
import java.util.stream.Collectors;

public class AllocationMapper {

    public static MatchingJobDto toJobDto(MatchingJob j) {
        MatchingJobDto d = new MatchingJobDto();
        d.setId(j.getId());
        d.setResourceRequestId(j.getResourceRequestId());
        d.setStatus(j.getStatus().name());
        d.setRequestedAt(j.getRequestedAt());
        d.setCreatedAt(j.getCreatedAt());
        return d;
    }

    public static CandidateDto toCandidateDto(Candidate c) {
        CandidateDto d = new CandidateDto();
        d.setId(c.getId());
        d.setMatchingJobId(c.getMatchingJobId());
        d.setResourceRequestId(c.getResourceRequestId());
        d.setEmployeeId(c.getEmployeeId());
        d.setScore(c.getScore());
        d.setSkillsMatched(c.getSkillsMatched());
        d.setExperienceDiff(c.getExperienceDiff());
        d.setAvailabilitySnapshot(c.getAvailabilitySnapshot());
        d.setLocationSnapshot(c.getLocationSnapshot());
        d.setStatus(c.getStatus().name());
        d.setSuggestedAt(c.getSuggestedAt());
        return d;
    }

    public static InterviewDto toInterviewDto(Interview i) {
        InterviewDto d = new InterviewDto();
        d.setId(i.getId());
        d.setCandidateId(i.getCandidateId());
        d.setScheduledByUserId(i.getScheduledByUserId());
        d.setScheduledAt(i.getScheduledAt());
        d.setMode(i.getMode());
        d.setInterviewerUserId(i.getInterviewerUserId());
        d.setResult(i.getResult().name());
        d.setFeedback(i.getFeedback());
        return d;
    }

    public static AllocationAttemptDto toAllocationAttemptDto(AllocationAttempt a) {
        AllocationAttemptDto d = new AllocationAttemptDto();
        d.setId(a.getId());
        d.setCandidateId(a.getCandidateId());
        d.setEmployeeId(a.getEmployeeId());
        d.setResourceRequestId(a.getResourceRequestId());
        d.setStatus(a.getStatus().name());
        d.setFailureReason(a.getFailureReason());
        d.setAttemptedAt(a.getAttemptedAt());
        return d;
    }
}
