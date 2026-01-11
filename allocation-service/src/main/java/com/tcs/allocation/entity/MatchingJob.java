package com.tcs.allocation.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "matching_job")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingJob {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long resourceRequestId;
    private Long requestedByUserId;
    private Instant requestedAt;
    @Enumerated(EnumType.STRING)
    private Status status;
    private boolean deleted = false;
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void pre() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        if (this.requestedAt == null) this.requestedAt = this.createdAt;
        if (this.status == null) this.status = Status.PENDING;
    }

    @PreUpdate
    public void preUpdate() { this.updatedAt = Instant.now(); }

    public enum Status { PENDING, RUNNING, COMPLETED, FAILED }
}
