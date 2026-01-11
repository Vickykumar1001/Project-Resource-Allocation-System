package com.tcs.allocation.dto;

import lombok.Data;

@Data
public class CandidateCreateDto {
    private Long employeeId;
    private Double matchScore;
    private Long suggestedByUserId;
    // other fields optional
}
