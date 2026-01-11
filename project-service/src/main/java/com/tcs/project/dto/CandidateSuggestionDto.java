package com.tcs.project.dto;

import lombok.Data;

@Data
public class CandidateSuggestionDto {
    private Long id;
    private Long resourceRequestId;
    private Long employeeId;
    private Long suggestedByUserId;
    private Double matchScore;
    private String status; // SUGGESTED, INTERVIEW_SCHEDULED, APPROVED, REJECTED
    private String managerFeedback;
}
