package com.tcs.allocation.service;

import com.tcs.allocation.client.EmployeeClient;
import com.tcs.allocation.client.ProjectClient;
import com.tcs.allocation.dto.*;
import com.tcs.allocation.entity.*;
import com.tcs.allocation.mapper.AllocationMapper;
import com.tcs.allocation.repository.*;
import com.tcs.allocation.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * MatchingService creates jobs and runs matching synchronously.
 */
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final MatchingJobRepository jobRepo;
    private final CandidateRepository candidateRepo;
    private final EmployeeClient employeeClient;
    private final ProjectClient projectClient;

    @Value("${matching.weight.skill:0.5}")
    private double wSkill;
    @Value("${matching.weight.experience:0.25}")
    private double wExp;
    @Value("${matching.weight.availability:0.15}")
    private double wAvail;
    @Value("${matching.weight.location:0.1}")
    private double wLoc;
    @Value("${matching.candidate.multiplier:4}")
    private int candidateMultiplier;

    @Transactional
    public MatchingJobDto createJob(MatchingJobCreateDto dto) {
        MatchingJob j = MatchingJob.builder()
                .resourceRequestId(dto.getResourceRequestId())
                .requestedByUserId(dto.getRequestedByUserId())
                .requestedAt(new Date().toInstant())
                .status(MatchingJob.Status.PENDING)
                .build();
        j = jobRepo.save(j);
        // run job immediately (sync). For long jobs consider @Async or scheduling
        runJob(j.getId(), dto.getFilters());
        return AllocationMapper.toJobDto(j);
    }

    public Page<MatchingJobDto> listJobs(Long resourceRequestId, Pageable pageable) {
        Page<MatchingJob> page = (resourceRequestId != null) ? jobRepo.findByResourceRequestIdAndDeletedFalse(resourceRequestId, pageable) : jobRepo.findAllByDeletedFalse(pageable);
        return page.map(AllocationMapper::toJobDto);
    }

    public MatchingJobDto getJob(Long id) {
        MatchingJob j = jobRepo.findById(id).orElseThrow(() -> new NotFoundException("Job not found: "+id));
        return AllocationMapper.toJobDto(j);
    }

    @Transactional
    public void runJob(Long jobId, Map<String,Object> filters) {
        MatchingJob job = jobRepo.findById(jobId).orElseThrow(() -> new NotFoundException("Job not found: " + jobId));
        job.setStatus(MatchingJob.Status.RUNNING);
        jobRepo.save(job);

        try {
            // 1) fetch resource request details from Project service (required skills, minExp, totalRequired)
            Map<String,Object> req = projectClient.getResourceRequest(job.getResourceRequestId());
            if (req == null) throw new NotFoundException("Resource request not found in Project service: " + job.getResourceRequestId());

            // Extract required fields (defensive)
            Integer totalRequired = req.get("totalRequired") == null ? 1 : (Integer) req.get("totalRequired");
            Integer minExp = req.get("minExperienceYears") == null ? 0 : (Integer) req.get("minExperienceYears");
            List<Map<String,Object>> skills = (List<Map<String,Object>>) req.getOrDefault("skills", List.of());

            // Build list of required skill names
            List<String> skillNames = skills.stream().map(s -> (String)s.get("skillName")).collect(Collectors.toList());

            // 2) fetch candidate pool from Employee service - for simplicity, we call search for each skill and union results
            Set<Map<String,Object>> pool = new LinkedHashSet<>();
            for (String skill : skillNames) {
                List<Map<String,Object>> page = employeeClient.searchEmployees(skill, minExp, 0, 200); // fetch up to 200 per skill
                if (page != null) pool.addAll(page);
            }
            // fallback: if no skill specified, fetch some employees (not implemented here)
            List<Map<String,Object>> poolList = new ArrayList<>(pool);

            // 3) score each candidate
            List<ScoredCandidate> scored = new ArrayList<>();
            for (Map<String,Object> emp : poolList) {
                try {
                    Long empId = ((Number)emp.get("id")).longValue();
                    Integer expYears = emp.get("experienceYears") == null ? 0 : ((Number)emp.get("experienceYears")).intValue();
                    String status = emp.getOrDefault("currentStatus","UNKNOWN").toString();
                    String location = emp.getOrDefault("location","").toString();
                    List<String> empSkills = (List<String>) emp.getOrDefault("skills", List.of());
                    // compute skill score: fraction of required skills present
                    int matched = 0;
                    for (String sk : skillNames) {
                        if (empSkills.stream().anyMatch(es -> es.equalsIgnoreCase(sk))) matched++;
                    }
                    double skillScore = skillNames.isEmpty() ? 0.0 : ((double)matched / (double)skillNames.size());
                    double expScore = 1.0;
                    if (minExp != null) {
                        int diff = expYears - minExp;
                        expScore = Math.max(0.0, 1.0 - (double)Math.abs(diff) / Math.max(1, minExp + 2));
                    }
                    double availScore = "AVAILABLE".equalsIgnoreCase(status) ? 1.0 : 0.0;
                    double locScore = 1.0; // conservative: treat location as neutral here unless filter provided
                    // weighted
                    double score = wSkill*skillScore + wExp*expScore + wAvail*availScore + wLoc*locScore;
                    scored.add(new ScoredCandidate(empId, score, matched, expYears - (minExp==null?0:minExp), status, location));
                } catch (Exception ex) {
                    // ignore problematic record
                }
            }

            // 4) sort and persist top N
            scored.sort((a,b) -> Double.compare(b.score, a.score));
            int topN = Math.max(1, totalRequired * candidateMultiplier);
            List<ScoredCandidate> top = scored.stream().limit(topN).collect(Collectors.toList());

            // persist Candidate entities
            for (ScoredCandidate sc : top) {
                Candidate c = Candidate.builder()
                        .matchingJobId(job.getId())
                        .resourceRequestId(job.getResourceRequestId())
                        .employeeId(sc.employeeId)
                        .score(sc.score)
                        .skillsMatched(sc.skillsMatched)
                        .experienceDiff(sc.experienceDiff)
                        .availabilitySnapshot(sc.availability)
                        .locationSnapshot(sc.location)
                        .status(Candidate.Status.SUGGESTED)
                        .build();
                candidateRepo.save(c);

                // also push suggestion to Project service
                CandidateCreateDto createDto = new CandidateCreateDto();
                createDto.setEmployeeId(sc.employeeId);
                createDto.setMatchScore(sc.score);
                createDto.setSuggestedByUserId(job.getRequestedByUserId());
                try {
                    projectClient.pushSuggestion(job.getResourceRequestId(), createDto);
                } catch (Exception ex) {
                    // log and continue; suggestion push is best-effort here
                }
            }

            job.setStatus(MatchingJob.Status.COMPLETED);
            jobRepo.save(job);
        } catch (Exception ex) {
            job.setStatus(MatchingJob.Status.FAILED);
            jobRepo.save(job);
            throw ex;
        }
    }

    private static class ScoredCandidate {
        Long employeeId;
        double score;
        int skillsMatched;
        int experienceDiff;
        String availability;
        String location;
        ScoredCandidate(Long employeeId, double score, int skillsMatched, int experienceDiff, String availability, String location){
            this.employeeId=employeeId; this.score=score; this.skillsMatched=skillsMatched; this.experienceDiff=experienceDiff; this.availability=availability; this.location=location;
        }
    }
}
