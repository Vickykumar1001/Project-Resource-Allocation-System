package com.tcs.employee.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "employee_profile", indexes = {
        @Index(name = "idx_user_id", columnList = "userId"),
        @Index(name = "idx_current_status", columnList = "currentStatus")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // id from auth service
    private Long userId;

    private Integer experienceYears;

    @Enumerated(EnumType.STRING)
    private CurrentStatus currentStatus;

    private Long currentProjectId; 

    private String location;

    private boolean deleted = false;

    private Instant createdAt;
    private Instant updatedAt;

    @Version
    private Long version;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EmployeeSkill> skills;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.currentStatus == null) this.currentStatus = CurrentStatus.AVAILABLE;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public static enum CurrentStatus {
        AVAILABLE,
        ALLOCATED,
        INACTIVE
    }
}
