package com.tcs.allocation.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class InterviewDto {
    private Long id;
    private Long candidateId;
    private Long scheduledByUserId;
    private Instant scheduledAt;
    private String mode;
    private Long interviewerUserId;
    private String result;
    private String feedback;
}
