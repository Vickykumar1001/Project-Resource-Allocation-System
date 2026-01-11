package com.tcs.allocation.client;

import com.tcs.allocation.dto.CandidateCreateDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "project-service", configuration = com.tcs.allocation.config.FeignConfig.class)
public interface ProjectClient {

    // push suggestions to Project service
    @PostMapping("/api/internal/resource-requests/{requestId}/suggest")
    Map<String,Object> pushSuggestion(@PathVariable("requestId") Long requestId, @RequestBody CandidateCreateDto dto);

    // optional: ask project for resource request details
    @GetMapping("/api/internal/resource-requests/{id}")
    Map<String,Object> getResourceRequest(@PathVariable("id") Long id);
}
