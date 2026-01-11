package com.tcs.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "candidate_suggestion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateSuggestion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // resource request for which candidate is suggested
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_request_id")
    private ResourceRequest resourceRequest;

    // employee id from Employee service
    private Long employeeId;

    // who suggested (RMG user id)
    private Long suggestedByUserId;

    // optional score computed by allocation/matching service
    private Double matchScore;

    @Enumerated(EnumType.STRING)
    private SuggestionStatus status;

    private String managerFeedback; // optional

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) this.status = SuggestionStatus.SUGGESTED;
    }

    @PreUpdate
    public void preUpdate() { this.updatedAt = Instant.now(); }

    public enum SuggestionStatus { SUGGESTED, INTERVIEW_SCHEDULED, APPROVED, REJECTED }
}
