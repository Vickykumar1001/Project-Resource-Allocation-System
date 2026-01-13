package com.tcs.employee.service;

import java.util.ArrayList;
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
import com.tcs.employee.exception.BadRequestException;
import com.tcs.employee.exception.ConflictException;
import com.tcs.employee.exception.NotFoundException;
import com.tcs.employee.exception.ServiceException;
import com.tcs.employee.mapper.EmployeeMapper;
import com.tcs.employee.repository.EmployeeProfileRepository;
import com.tcs.employee.repository.EmployeeSkillRepository;
import com.tcs.employee.repository.SkillRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeProfileRepository profileRepo;
    private final SkillRepository skillRepo;
    private final EmployeeSkillRepository employeeSkillRepo;
    private final AuthClient authClient;

    @Override
    @Transactional
    public EmployeeResponseDto createEmployee(EmployeeCreateRequest req) {
        UserDto user = fetchUserDtoByToken();
        if (user == null) {
            throw new BadRequestException("No user exists for provided token");
        }
        // use id from token - override incoming
        req.setUserId(user.getId());

        profileRepo.findByUserIdAndDeletedFalse(req.getUserId()).ifPresent(p -> {
            throw new ConflictException("Employee profile already exists for userId: " + req.getUserId());
        });

        var profile = EmployeeMapper.fromCreateRequest(req);
        profile = profileRepo.save(profile);

        if (req.getSkills() != null && !req.getSkills().isEmpty()) {
            attachSkills(profile, req.getSkills());
        }

        return EmployeeMapper.toResponseDto(profile, user);
    }

    @Override
    @Transactional
    public EmployeeResponseDto updateEmployee(Long id, EmployeeUpdateRequest req) {
        EmployeeProfile profile = profileRepo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + id));

        if (req.getExperienceYears() != null) profile.setExperienceYears(req.getExperienceYears());
        if (req.getLocation() != null) profile.setLocation(req.getLocation());

        profile = profileRepo.save(profile);

        if (req.getSkills() != null) {
            // delete previous skills for this employee
            List<EmployeeSkill> existing = employeeSkillRepo.findByEmployee_Id(profile.getId());
            if (existing != null && !existing.isEmpty()) {
                employeeSkillRepo.deleteAll(existing);
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
        // set skills from repository (not from global findAll)
        profile.setSkills(employeeSkillRepo.findByEmployee_Id(profile.getId()));
    }

    @Override
    public EmployeeResponseDto getById(Long id) {
        EmployeeProfile p = profileRepo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + id));
        UserDto user = fetchUserDto(p.getUserId());
        return EmployeeMapper.toResponseDto(p, user);
    }

    @Override
    public Page<EmployeeResponseDto> list(String skill, Integer minExp, String status,
                                          String location, Pageable pageable) {

        Page<EmployeeProfile> page;
        if (skill != null && !skill.isBlank()) {
            page = profileRepo.findBySkills_Skill_NameIgnoreCaseAndDeletedFalse(skill, pageable);
        } else {
            page = profileRepo.findAllByDeletedFalse(pageable);
        }

        // filter by minExp / status / location 
        List<EmployeeProfile> filtered = page.getContent().stream()
                .filter(p -> minExp == null || (p.getExperienceYears() != null && p.getExperienceYears() >= minExp))
                .filter(p -> status == null || p.getCurrentStatus().name().equalsIgnoreCase(status))
                .filter(p -> location == null || p.getLocation() == null || p.getLocation().equalsIgnoreCase(location))
                .collect(Collectors.toList());

        List<Long> userIds = filtered.stream().map(EmployeeProfile::getUserId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, UserDto> userMap = fetchUserMap(userIds);

        List<EmployeeResponseDto> dtos = filtered.stream()
                .map(p -> EmployeeMapper.toResponseDto(p, userMap.get(p.getUserId())))
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }


    @Override
    @Transactional
    public boolean claimForAllocation(Long employeeId, Long projectId) {
        EmployeeProfile p = profileRepo.findByIdAndDeletedFalse(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + employeeId));

        if (p.getCurrentStatus() != EmployeeProfile.CurrentStatus.AVAILABLE) {
            return false;
        }
        p.setCurrentStatus(EmployeeProfile.CurrentStatus.ALLOCATED);
        p.setCurrentProjectId(projectId);
        profileRepo.save(p);
        return true;
    }

    @Override
    @Transactional
    public void releaseAllocation(Long employeeId) {
        EmployeeProfile p = profileRepo.findByIdAndDeletedFalse(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + employeeId));
        p.setCurrentStatus(EmployeeProfile.CurrentStatus.AVAILABLE);
        p.setCurrentProjectId(null);
        profileRepo.save(p);
    }

    private UserDto fetchUserDto(Long userId) {
        if (userId == null) return null;
        String ids = String.valueOf(userId);
        List<UserDto> list;
        try {
            list = authClient.getUsersByIds(ids);
        } catch (Exception ex) {
            throw new ServiceException("Failed calling auth service to fetch user: " + userId, ex);
        }
        if (list == null || list.isEmpty()) return null;
        return list.get(0);
    }

    private UserDto fetchUserDtoByToken() {
        try {
            ResponseEntity<UserDto> usr = authClient.getUserFromToken();
            return usr.getBody();
        } catch (Exception ex) {
            throw new ServiceException("Failed to fetch user from token", ex);
        }
    }

    private Map<Long, UserDto> fetchUserMap(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return Map.of();
        String ids = userIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        List<UserDto> list;
        try {
            list = authClient.getUsersByIds(ids);
        } catch (Exception ex) {
            throw new ServiceException("Failed calling auth service to fetch users", ex);
        }
        if (list == null) return Map.of();
        return list.stream().collect(Collectors.toMap(UserDto::getId, u -> u));
    }

    @Override
    public List<EmployeeResponseDto> getByIds(List<Long> ids) {
        List<com.tcs.employee.entity.EmployeeProfile> profiles = profileRepo.findAllByIdInAndDeletedFalse(ids);
        List<Long> userIds = profiles.stream().map(EmployeeProfile::getUserId).distinct().collect(Collectors.toList());
        Map<Long, UserDto> userMap = fetchUserMap(userIds);
        return profiles.stream().map(p -> EmployeeMapper.toResponseDto(p, userMap.get(p.getUserId()))).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        EmployeeProfile p = profileRepo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + id));

        p.setDeleted(true);
        p.setCurrentStatus(EmployeeProfile.CurrentStatus.INACTIVE);
        p.setCurrentProjectId(null);

        profileRepo.save(p);

        Long userId = p.getUserId();
        if (userId != null) {
            try {
                authClient.softDeleteUser(userId);
            } catch (Exception ex) {
                throw new ServiceException("Failed to delete user from auth service for userId: " + userId, ex);
            }
        }
    }
}
