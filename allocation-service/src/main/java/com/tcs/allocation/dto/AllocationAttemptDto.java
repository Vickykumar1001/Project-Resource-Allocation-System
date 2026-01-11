package com.tcs.allocation.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class AllocationAttemptDto {
    private Long id;
    private Long candidateId;
    private Long employeeId;
    private Long resourceRequestId;
    private String status;
    private String failureReason;
    private Instant attemptedAt;
}
