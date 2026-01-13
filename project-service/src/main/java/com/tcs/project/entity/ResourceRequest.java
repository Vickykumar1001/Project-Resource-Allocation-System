package com.tcs.project.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
public class ResourceRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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

	@OneToMany(mappedBy = "resourceRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ResourceSkillRequirement> skillRequirements = new ArrayList<>();

	@PrePersist
	public void prePersist() {
		this.createdAt = Instant.now();
		this.updatedAt = this.createdAt;
		if (this.status == null)
			this.status = RequestStatus.OPEN;
		if (this.priority == null)
			this.priority = Priority.MEDIUM;
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = Instant.now();
	}

	public enum Priority {
		LOW, MEDIUM, HIGH
	}

	public enum RequestStatus {
		OPEN, IN_PROGRESS, PARTIALLY_FULFILLED, FULFILLED, CLOSED
	}
}
