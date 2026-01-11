package com.tcs.project.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

@Data
public class ProjectCreateDto {
    @NotBlank
    private String projectName;
    private String projectCode;
    private String description;
    private Long managerUserId;
    private Instant startDate;
    private Instant endDate;
}
