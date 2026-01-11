package com.tcs.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String projectCode; // e.g. PRJ-001
    private String projectName;
    @Column(length = 2000)
    private String description;

    // manager user id from Auth service
    private Long managerUserId;

    private Instant startDate;
    private Instant endDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    private boolean deleted = false;

    private Instant createdAt;
    private Instant updatedAt;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ResourceRequest> resourceRequests = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        if (this.status == null) this.status = Status.PLANNED;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public enum Status {
        PLANNED, ACTIVE, ON_HOLD, COMPLETED, CANCELLED
    }
}
