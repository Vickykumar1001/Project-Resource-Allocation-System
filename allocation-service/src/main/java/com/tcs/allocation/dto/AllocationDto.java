package com.tcs.allocation.dto;

import java.time.Instant;

import lombok.Data;

@Data
public class AllocationDto {
    private Long id;
    private Long employeeId;
    private Long projectId;
    private Long resourceRequestId;
    private Long allocatedByUserId;
    private Instant allocatedAt;
    private String status;
}
