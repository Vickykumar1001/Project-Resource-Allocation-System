package com.tcs.allocation.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class MatchingJobDto {
    private Long id;
    private Long resourceRequestId;
    private String status;
    private Instant requestedAt;
    private Instant createdAt;
}
