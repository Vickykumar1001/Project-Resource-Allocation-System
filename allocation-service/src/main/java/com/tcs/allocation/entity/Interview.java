package com.tcs.allocation.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "interview", indexes = {@Index(columnList = "candidateId")})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long candidateId;
    private Long scheduledByUserId;
    private Instant scheduledAt;
    private String mode;
    private Long interviewerUserId;

    @Enumerated(EnumType.STRING)
    private Result result;

    private String feedback;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void pre() { this.createdAt = Instant.now(); this.updatedAt = this.createdAt; if (this.result==null) this.result=Result.PENDING; }
    @PreUpdate
    public void preUpdate() { this.updatedAt = Instant.now();}

    public enum Result { PENDING, PASSED, FAILED, CANCELLED }
}
