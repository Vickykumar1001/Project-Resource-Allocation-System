package com.tcs.project.dto;

import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
public class ProjectResponseDto {
    private Long id;
    private String projectCode;
    private String projectName;
    private String description;
    private Long managerUserId;
    private Instant startDate;
    private Instant endDate;
    private String status;
    private List<ResourceRequestResponseDto> resourceRequests;
}
