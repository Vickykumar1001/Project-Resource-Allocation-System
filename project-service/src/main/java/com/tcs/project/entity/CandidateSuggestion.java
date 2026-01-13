package com.tcs.project.entity;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateSuggestion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// resource request for which candidate is suggested
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "resource_request_id")
	private ResourceRequest resourceRequest;

	// employee id from employee service
	private Long employeeId;

	// who suggested (RMG user id)
	private Long suggestedByUserId;

	//score computed by allocation/matching service
	private Double matchScore;

	@Enumerated(EnumType.STRING)
	private SuggestionStatus status;

	private String managerFeedback;

	private Instant createdAt;
	private Instant updatedAt;

	@PrePersist
	public void prePersist() {
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
		if (this.status == null)
			this.status = SuggestionStatus.SUGGESTED;
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = Instant.now();
	}

	public enum SuggestionStatus {
		SUGGESTED, APPROVED, REJECTED
	}
}
