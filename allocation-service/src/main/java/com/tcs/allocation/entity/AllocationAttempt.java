package com.tcs.allocation.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "allocation_attempt", indexes = {
        @Index(columnList = "resourceRequestId"),
        @Index(columnList = "employeeId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllocationAttempt {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long candidateId;
    private Long employeeId;
    private Long resourceRequestId;
    private Long matchingJobId;
    private Long attemptedByUserId;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String failureReason;
    private Instant attemptedAt;
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void pre() { this.createdAt = Instant.now(); this.updatedAt = this.createdAt; if (this.attemptedAt==null) this.attemptedAt=this.createdAt; if (this.status==null) this.status=Status.PENDING; }
    @PreUpdate
    public void preUpdate() { this.updatedAt = Instant.now();}

    public enum Status { PENDING, SUCCESS, FAILED }
}
