package com.tcs.employee.entity;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
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
