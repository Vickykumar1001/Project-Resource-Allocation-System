package com.tcs.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "allocation_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllocationRecord {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long employeeId;
    private Long projectId;
    private Long resourceRequestId;

    private Long allocatedByUserId; // RMG who performed allocation
    private Instant allocatedAt;

    @Enumerated(EnumType.STRING)
    private AllocationStatus status;

    @PrePersist
    public void prePersist() {
        if (this.allocatedAt == null) this.allocatedAt = Instant.now();
        if (this.status == null) this.status = AllocationStatus.ALLOCATED;
    }

    public enum AllocationStatus { ALLOCATED, RELEASED }
}
