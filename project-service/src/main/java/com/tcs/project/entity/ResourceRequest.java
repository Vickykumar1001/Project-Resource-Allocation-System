package com.tcs.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resource_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceRequest {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    private Long requestedByUserId; // manager who created

    private Integer totalRequired;
    private Integer minExperienceYears;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    private boolean deleted = false;

    private Instant createdAt;
    private Instant updatedAt;

    @OneToMany(mappedBy = "resourceRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ResourceSkillRequirement> skillRequirements = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        if (this.status == null) this.status = RequestStatus.OPEN;
        if (this.priority == null) this.priority = Priority.MEDIUM;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public enum Priority { LOW, MEDIUM, HIGH }

    public enum RequestStatus { OPEN, IN_PROGRESS, PARTIALLY_FULFILLED, FULFILLED, CLOSED }
}
