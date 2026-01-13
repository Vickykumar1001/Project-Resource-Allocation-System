package com.tcs.allocation.service;

import java.util.List;

import com.tcs.allocation.dto.AllocationDto;
import com.tcs.allocation.dto.CandidateDto;

public interface AllocationService {
    List<CandidateDto> suggestCandidates(Long resourceRequestId);
    AllocationDto allocate(Long resourceRequestId, Long employeeId, Long allocatedByUserId);
    AllocationDto release(Long allocationId);
}
