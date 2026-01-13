package com.tcs.allocation.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.tcs.allocation.client.EmployeeClient;
import com.tcs.allocation.client.ProjectClient;
import com.tcs.allocation.dto.AllocationDto;
import com.tcs.allocation.dto.CandidateDto;
import com.tcs.allocation.dto.EmployeeResponseDto;
import com.tcs.allocation.dto.ResourceRequestResponseDto;
import com.tcs.allocation.dto.SkillRequest;
import com.tcs.allocation.dto.SkillRequirementDto;
import com.tcs.allocation.exception.ExternalServiceException;
import com.tcs.allocation.exception.NotFoundException;

@Service
public class AllocationServiceImpl implements AllocationService {

	private static final Logger log = LoggerFactory.getLogger(AllocationServiceImpl.class);

	private final EmployeeClient eclient;
	private final ProjectClient pclient;

	public AllocationServiceImpl(EmployeeClient eclient, ProjectClient pclient) {
		this.eclient = eclient;
		this.pclient = pclient;
	}

	@Override
	public List<CandidateDto> suggestCandidates(Long resourceRequestId) {
	    // 1. fetch request
	    ResourceRequestResponseDto req = fetchResourceRequestOrThrow(resourceRequestId);

	    // 2. fetch available employees (page size large enough)
	    ResponseEntity<List<EmployeeResponseDto>> resp;
	    try {
	        resp = eclient.list(null, null, "AVAILABLE", null, 0, 200);
	    } catch (Exception ex) {
	        log.error("Failed to call employee-service to list employees: {}", ex.getMessage(), ex);
	        throw new ExternalServiceException("Cannot fetch employees from employee service", ex);
	    }

	    // Validate response
	    if (resp == null) {
	        throw new ExternalServiceException("Employee service returned null response");
	    }
	    if (!resp.getStatusCode().is2xxSuccessful()) {
	        throw new ExternalServiceException("Employee service returned non-success status: " + resp.getStatusCode());
	    }

	    List<EmployeeResponseDto> employees = resp.getBody();
	    if (employees == null || employees.isEmpty()) {
	        throw new NotFoundException("No employees returned by employee service");
	    }

	    // compute score for each employee and sort descending
	    List<CandidateDto> candidates = employees.stream()
	            .map(emp -> {
	                double score = computeMatchingScore(emp, req);
	                CandidateDto c = new CandidateDto();
	                c.setEmployee(emp);
	                c.setScore(score);
	                return c;
	            })
	            .sorted(Comparator.comparingDouble(CandidateDto::getScore).reversed())
	            .collect(Collectors.toList());

	    return candidates;
	}


	@Override
	public AllocationDto allocate(Long resourceRequestId, Long employeeId, Long allocatedByUserId) {
		// call project-service to allocate
		AllocationDto allocation;
		try {
			ResponseEntity<AllocationDto> resp = pclient.allocate(resourceRequestId, employeeId, allocatedByUserId);
			allocation = resp == null ? null : resp.getBody();
		} catch (Exception ex) {

			throw new ExternalServiceException("Failed to allocate via project service", ex);
		}

		if (allocation == null) {
			throw new NotFoundException("Allocation failed - no data returned");
		}

		try {
			eclient.claimEmployee(employeeId, allocation.getProjectId());
		} catch (Exception ex) {
			throw new ExternalServiceException("Allocation succeeded but failed to claim employee", ex);
		}

		return allocation;
	}

	@Override
	public AllocationDto release(Long allocationId) {
		// 1. fetch allocation
		AllocationDto allocation;
		try {
			ResponseEntity<AllocationDto> resp = pclient.getAllocation(allocationId);
			allocation = resp == null ? null : resp.getBody();
		} catch (Exception ex) {
			log.error("Failed to fetch allocation {}: {}", allocationId, ex.getMessage(), ex);
			throw new ExternalServiceException("Failed to fetch allocation", ex);
		}

		if (allocation == null) {
			throw new NotFoundException("Allocation not found: " + allocationId);
		}

		// 2. call project-service to release
		try {
			pclient.release(allocationId);
		} catch (Exception ex) {
			log.error("Failed to call project service release for {}: {}", allocationId, ex.getMessage(), ex);
			throw new ExternalServiceException("Failed to release allocation in project service", ex);
		}

		try {
			if (allocation.getEmployeeId() != null) {
				eclient.releaseEmployee(allocation.getEmployeeId());
			}
		} catch (Exception ex) {
			throw new ExternalServiceException("Released allocation but failed to release employee in employee service",
					ex);
		}

		return allocation;
	}

	private ResourceRequestResponseDto fetchResourceRequestOrThrow(Long resourceRequestId) {
		try {
			ResponseEntity<ResourceRequestResponseDto> resp = pclient.getRequest(resourceRequestId);
			ResourceRequestResponseDto req = resp == null ? null : resp.getBody();
			if (req == null)
				throw new NotFoundException("Resource request not found: " + resourceRequestId);
			return req;
		} catch (NotFoundException nf) {
			throw nf;
		} catch (Exception ex) {
			throw new ExternalServiceException("Failed to fetch resource request", ex);
		}
	}

	/**
	 * Compute matching score (0..100) using: - Experience portion: up to 40 points
	 * based on ratio of employee exp to required min exp. - Skills portion: up to
	 * 60 points. Each required skill shares weight equally. For each skill,
	 * contribution = weightPerSkill * min(1.0, (empProfNumeric / reqProfNumeric)).
	 * If no required skills, employee granted full skill points.
	 */
	private double computeMatchingScore(EmployeeResponseDto emp, ResourceRequestResponseDto req) {
		double score = 0.0;

		int reqMinExp = req.getMinExperienceYears() != null ? req.getMinExperienceYears() : 0;
		int empExp = emp.getExperienceYears() != null ? emp.getExperienceYears() : 0;

		// experience (0..40)
		if (reqMinExp <= 0) {
			score += 40;
		} else {
			double ratio = Math.min(1.0, (double) empExp / reqMinExp);
			score += ratio * 40.0;
		}

		// skills (0..60)
		List<SkillRequirementDto> reqSkills = req.getSkills();
		List<SkillRequest> empSkills = emp.getSkills();

		if (reqSkills == null || reqSkills.isEmpty()) {
			score += 60;
		} else {
			int totalReq = reqSkills.size();
			double weightPerSkill = 60.0 / totalReq;
			double skillTotal = 0.0;

			// build map of employee skill name -> proficiency numeric
			Map<String, Integer> empSkillMap = new HashMap<>();
			if (empSkills != null) {
				for (SkillRequest s : empSkills) {
					if (s.getName() != null) {
						empSkillMap.put(s.getName().toLowerCase(), proficiencyToNumber(s.getProficiency()));
					}
				}
			}

			for (SkillRequirementDto r : reqSkills) {
				String reqName = r.getSkillName();
				if (reqName == null) {
					skillTotal += weightPerSkill; // assume satisfied
					continue;
				}
				String key = reqName.toLowerCase();
				Integer empProf = empSkillMap.get(key);
				int reqProf = proficiencyStringToNumber(r.getMinProficiency());

				if (empProf == null) {
					// missing skill -> 0 for this skill
					continue;
				} else {
					double ratio = reqProf <= 0 ? 1.0 : ((double) empProf / reqProf);
					ratio = Math.min(1.0, ratio);
					skillTotal += weightPerSkill * ratio;
				}
			}
			score += skillTotal;
		}

		if (score > 100)
			score = 100;
		return Math.round(score * 100.0) / 100.0;
	}

	private int proficiencyToNumber(SkillRequest.Proficiency p) {
		if (p == null)
			return 1;
		switch (p) {
		case BEGINNER:
			return 1;
		case INTERMEDIATE:
			return 2;
		case EXPERT:
			return 3;
		default:
			return 1;
		}
	}

	private int proficiencyStringToNumber(String s) {
		if (s == null)
			return 1;
		String normalized = s.trim().toUpperCase();
		switch (normalized) {
		case "BEGINNER":
			return 1;
		case "INTERMEDIATE":
			return 2;
		case "EXPERT":
			return 3;
		default:
			try {
				return Integer.parseInt(normalized);
			} catch (Exception e) {
				return 1;
			}
		}
	}
}
