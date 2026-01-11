package com.tcs.employee.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tcs.employee.client.AuthClient;
import com.tcs.employee.dto.EmployeeCreateRequest;
import com.tcs.employee.dto.EmployeeResponseDto;
import com.tcs.employee.dto.EmployeeUpdateRequest;
import com.tcs.employee.dto.SkillRequest;
import com.tcs.employee.dto.UserDto;
import com.tcs.employee.entity.EmployeeProfile;
import com.tcs.employee.entity.EmployeeSkill;
import com.tcs.employee.entity.EmployeeSkillId;
import com.tcs.employee.entity.Skill;
import com.tcs.employee.exception.NotFoundException;
import com.tcs.employee.mapper.EmployeeMapper;
import com.tcs.employee.repository.EmployeeProfileRepository;
import com.tcs.employee.repository.EmployeeSkillRepository;
import com.tcs.employee.repository.SkillRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeProfileRepository profileRepo;
    private final SkillRepository skillRepo;
    private final EmployeeSkillRepository employeeSkillRepo;
    private final AuthClient authClient;

    @Transactional
    public EmployeeResponseDto createEmployee(EmployeeCreateRequest req) {
        // ensure userId doesn't already have profile
        UserDto user=fetchUserDtoByToken();
        if(user==null) {
        	throw new IllegalArgumentException("No user exists for userId: " + req.getUserId());
        }
        req.setUserId(user.getId());
        
        profileRepo.findByUserIdAndDeletedFalse(req.getUserId()).ifPresent(p -> {
            throw new IllegalArgumentException("Employee profile already exists for userId: " + req.getUserId());
        });
        

        EmployeeProfile profile = EmployeeMapper.fromCreateRequest(req);
        profile = profileRepo.save(profile);

        if (req.getSkills() != null && !req.getSkills().isEmpty()) {
            attachSkills(profile, req.getSkills());
        }

        return EmployeeMapper.toResponseDto(profile, user);
    }

    @Transactional
    public EmployeeResponseDto updateEmployee(Long id, EmployeeUpdateRequest req) {
    	
        EmployeeProfile profile = profileRepo.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new NotFoundException("Employee not found: " + id));

        if (req.getExperienceYears() != null) profile.setExperienceYears(req.getExperienceYears());
        if (req.getLocation() != null) profile.setLocation(req.getLocation());
        profile = profileRepo.save(profile);

        // replace skills if provided
        if (req.getSkills() != null) {
            // remove existing
            if (profile.getSkills() != null) {
                employeeSkillRepo.deleteAll(profile.getSkills());
                profile.getSkills().clear();
            }
            attachSkills(profile, req.getSkills());
        }

        UserDto user = fetchUserDto(profile.getUserId());
        return EmployeeMapper.toResponseDto(profile, user);
    }

    private void attachSkills(EmployeeProfile profile, List<SkillRequest> skills) {
        List<EmployeeSkill> toSave = new ArrayList<>();
        for (SkillRequest sreq : skills) {
            Skill skill = skillRepo.findByNameIgnoreCase(sreq.getName())
                    .orElseGet(() -> skillRepo.save(new Skill(null, sreq.getName())));
            EmployeeSkill es = new EmployeeSkill();
            EmployeeSkillId id = new EmployeeSkillId(profile.getId(), skill.getId());
            es.setId(id);
            es.setEmployee(profile);
            es.setSkill(skill);
            es.setProficiency(sreq.getProficiency());
            toSave.add(es);
        }
        employeeSkillRepo.saveAll(toSave);
        // refresh profile.skills
        profile.setSkills(employeeSkillRepo.findAll().stream()
                .filter(es -> es.getEmployee().getId().equals(profile.getId()))
                .collect(Collectors.toList()));
    }

    public EmployeeResponseDto getById(Long id) {
        EmployeeProfile p = profileRepo.findById(id)
                .filter(x -> !x.isDeleted())
                .orElseThrow(() -> new NotFoundException("Employee not found: " + id));
        UserDto user = fetchUserDto(p.getUserId());
        return EmployeeMapper.toResponseDto(p, user);
    }

    public Page<EmployeeResponseDto> list(String skill, Integer minExp, EmployeeProfile.CurrentStatus status,
                                          String location, Pageable pageable) {

        Page<EmployeeProfile> page;
        if (skill != null && !skill.isBlank()) {
            page = profileRepo.findBySkillName(skill, pageable);
        } else {
            page = profileRepo.findAllByDeletedFalse(pageable);
        }

        // Post-filter by minExp / status / location (simple approach)
        List<EmployeeProfile> filtered = page.getContent().stream()
                .filter(p -> minExp == null || (p.getExperienceYears() != null && p.getExperienceYears() >= minExp))
                .filter(p -> status == null || p.getCurrentStatus() == status)
                .filter(p -> location == null || p.getLocation() == null || p.getLocation().equalsIgnoreCase(location))
                .collect(Collectors.toList());

        // fetch userDtos in batch
        List<Long> userIds = filtered.stream().map(EmployeeProfile::getUserId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, UserDto> userMap = fetchUserMap(userIds);

        List<EmployeeResponseDto> dtos = filtered.stream()
                .map(p -> EmployeeMapper.toResponseDto(p, userMap.get(p.getUserId())))
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Transactional
    public EmployeeResponseDto setStatus(Long id, EmployeeProfile.CurrentStatus newStatus) {
        EmployeeProfile p = profileRepo.findById(id)
                .filter(x -> !x.isDeleted())
                .orElseThrow(() -> new NotFoundException("Employee not found: " + id));
        p.setCurrentStatus(newStatus);
        if (newStatus != EmployeeProfile.CurrentStatus.ALLOCATED) p.setCurrentProjectId(null);
        profileRepo.save(p);
        return EmployeeMapper.toResponseDto(p, fetchUserDto(p.getUserId()));
    }

    /**
     * Claim employee for allocation. Atomic: uses optimistic locking via @Version.
     * Returns true if claim succeeded.
     */
    @Transactional
    public boolean claimForAllocation(Long employeeId, Long projectId) {
        EmployeeProfile p = profileRepo.findById(employeeId)
                .filter(x -> !x.isDeleted())
                .orElseThrow(() -> new NotFoundException("Employee not found: " + employeeId));

        if (p.getCurrentStatus() != EmployeeProfile.CurrentStatus.AVAILABLE) {
            return false;
        }
        p.setCurrentStatus(EmployeeProfile.CurrentStatus.ALLOCATED);
        p.setCurrentProjectId(projectId);
        profileRepo.save(p);
        return true;
    }

    @Transactional
    public void releaseAllocation(Long employeeId) {
        EmployeeProfile p = profileRepo.findById(employeeId)
                .filter(x -> !x.isDeleted())
                .orElseThrow(() -> new NotFoundException("Employee not found: " + employeeId));
        p.setCurrentStatus(EmployeeProfile.CurrentStatus.AVAILABLE);
        p.setCurrentProjectId(null);
        profileRepo.save(p);
    }

    // helper: fetch single user via Feign batch call
    private UserDto fetchUserDto(Long userId) {
        if (userId == null) return null;
        String ids = String.valueOf(userId);
        List<UserDto> list = authClient.getUsersByIds(ids);
        if (list == null || list.isEmpty()) return null;
        return list.get(0);
    }
    
    private UserDto fetchUserDtoByToken() {
        ResponseEntity<UserDto> usr = authClient.getUserFromToken();
        return usr.getBody();
    }

    private Map<Long, UserDto> fetchUserMap(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return Collections.emptyMap();
        String ids = userIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        List<UserDto> list = authClient.getUsersByIds(ids);
        if (list == null) return Collections.emptyMap();
        return list.stream().collect(Collectors.toMap(UserDto::getId, u -> u));
    }

    public List<EmployeeResponseDto> getByIds(List<Long> ids) {
        List<EmployeeProfile> profiles = profileRepo.findAllByIdInAndDeletedFalse(ids);
        List<Long> userIds = profiles.stream().map(EmployeeProfile::getUserId).distinct().collect(Collectors.toList());
        Map<Long, UserDto> userMap = fetchUserMap(userIds);
        return profiles.stream().map(p -> EmployeeMapper.toResponseDto(p, userMap.get(p.getUserId()))).collect(Collectors.toList());
    }
}
