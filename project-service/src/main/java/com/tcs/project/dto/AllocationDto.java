package com.tcs.project.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class AllocationDto {
    private Long id;
    private Long employeeId;
    private Long projectId;
    private Long resourceRequestId;
    private Long allocatedByUserId;
    private Instant allocatedAt;
    private String status; // ALLOCATED / RELEASED
}
