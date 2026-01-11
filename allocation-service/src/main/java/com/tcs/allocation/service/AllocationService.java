package com.tcs.allocation.service;

import com.tcs.allocation.client.EmployeeClient;
import com.tcs.allocation.client.ProjectClient;
import com.tcs.allocation.entity.*;
import com.tcs.allocation.dto.AllocationAttemptDto;
import com.tcs.allocation.mapper.AllocationMapper;
import com.tcs.allocation.repository.*;
import com.tcs.allocation.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AllocationService {

    private final AllocationAttemptRepository allocationRepo;
    private final CandidateRepository candidateRepo;
    private final ProjectClient projectClient;
    private final EmployeeClient employeeClient;

    /**
     * Attempt allocation: claim employee -> push allocation to Project -> record attempt
     */
    @Transactional
    public AllocationAttemptDto allocateCandidate(Long candidateId, Long allocatedByUserId) {
        Candidate c = candidateRepo.findById(candidateId).orElseThrow(() -> new NotFoundException("Candidate not found: "+candidateId));
        Long employeeId = c.getEmployeeId();
        Long requestId = c.getResourceRequestId();

        AllocationAttempt attempt = AllocationAttempt.builder()
                .candidateId(candidateId)
                .employeeId(employeeId)
                .resourceRequestId(requestId)
                .matchingJobId(c.getMatchingJobId())
                .attemptedByUserId(allocatedByUserId)
                .status(AllocationAttempt.Status.PENDING)
                .attemptedAt(Instant.now())
                .build();
        attempt = allocationRepo.save(attempt);

        // 1) claim employee
        try {
            employeeClient.claimEmployee(employeeId, c.getResourceRequestId()); // may throw FeignException
        } catch (Exception ex) {
            attempt.setStatus(AllocationAttempt.Status.FAILED);
            attempt.setFailureReason("Claim failed: "+ex.getMessage());
            allocationRepo.save(attempt);
            c.setStatus(Candidate.Status.ALLOCATION_FAILED);
            candidateRepo.save(c);
            return AllocationMapper.toAllocationAttemptDto(attempt);
        }

        // 2) inform Project service to record allocation (best-effort)
        try {
            // call project service allocate endpoint
            projectClient.getResourceRequest(requestId); // ensure exists; real call should be allocate endpoint
            // For demo we assume project service records allocation elsewhere. We skip using a dedicated allocate call to avoid coupling.
            attempt.setStatus(AllocationAttempt.Status.SUCCESS);
            attempt = allocationRepo.save(attempt);
            c.setStatus(Candidate.Status.ALLOCATED);
            candidateRepo.save(c);
            return AllocationMapper.toAllocationAttemptDto(attempt);
        } catch (Exception ex) {
            // compensation: release employee
            try { employeeClient.releaseEmployee(employeeId); } catch(Exception e){}
            attempt.setStatus(AllocationAttempt.Status.FAILED);
            attempt.setFailureReason("Project record failed: "+ex.getMessage());
            allocationRepo.save(attempt);
            c.setStatus(Candidate.Status.ALLOCATION_FAILED);
            candidateRepo.save(c);
            return AllocationMapper.toAllocationAttemptDto(attempt);
        }
    }

    public List<AllocationAttemptDto> listAllocationsByRequest(Long requestId) {
        return allocationRepo.findByResourceRequestId(requestId).stream().map(AllocationMapper::toAllocationAttemptDto).collect(Collectors.toList());
    }
}
