package com.tcs.project.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
    private long timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<String> errors;
}
