package com.tcs.allocation.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "candidate", indexes = {
        @Index(columnList = "matchingJobId"),
        @Index(columnList = "resourceRequestId"),
        @Index(columnList = "employeeId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long matchingJobId;
    private Long resourceRequestId;
    private Long employeeId;

    private Double score;
    private Integer skillsMatched;
    private Integer experienceDiff;
    private String availabilitySnapshot;
    private String locationSnapshot;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String managerFeedback;

    private boolean deleted = false;
    private Instant suggestedAt;
    private Instant updatedAt;

    @PrePersist
    public void pre() {
        this.suggestedAt = Instant.now();
        this.updatedAt = this.suggestedAt;
        if (this.status == null) this.status = Status.SUGGESTED;
    }

    @PreUpdate
    public void preUpdate() { this.updatedAt = Instant.now(); }

    public enum Status {
        SUGGESTED,
        INTERVIEW_SCHEDULED,
        INTERVIEW_PASSED,
        INTERVIEW_FAILED,
        APPROVED_BY_MANAGER,
        REJECTED_BY_MANAGER,
        ALLOCATING,
        ALLOCATED,
        ALLOCATION_FAILED
    }
}
