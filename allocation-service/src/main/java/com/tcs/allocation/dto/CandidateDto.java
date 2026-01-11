package com.tcs.allocation.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class CandidateDto {
    private Long id;
    private Long matchingJobId;
    private Long resourceRequestId;
    private Long employeeId;
    private Double score;
    private Integer skillsMatched;
    private Integer experienceDiff;
    private String availabilitySnapshot;
    private String locationSnapshot;
    private String status;
    private Instant suggestedAt;
}
