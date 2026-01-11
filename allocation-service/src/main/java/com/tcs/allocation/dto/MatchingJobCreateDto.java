package com.tcs.allocation.dto;

import lombok.Data;
import java.util.Map;

@Data
public class MatchingJobCreateDto {
    private Long resourceRequestId;
    private Long requestedByUserId;
    private Map<String,Object> filters;
}
