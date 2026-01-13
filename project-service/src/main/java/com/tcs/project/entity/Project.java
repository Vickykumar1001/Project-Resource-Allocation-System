package com.tcs.project.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class Project {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String projectCode;
	private String projectName;
	@Column(length = 2000)
	private String description;

	// manager user id from auth service
	private Long managerUserId;

	private Instant startDate;
	private Instant endDate;

	@Enumerated(EnumType.STRING)
	private Status status;

	private boolean deleted = false;

	private Instant createdAt;
	private Instant updatedAt;

	@OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ResourceRequest> resourceRequests = new ArrayList<>();

	@PrePersist
	public void prePersist() {
		this.createdAt = Instant.now();
		this.updatedAt = this.createdAt;
		if (this.status == null)
			this.status = Status.PLANNED;
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = Instant.now();
	}

	public enum Status {
		PLANNED, ACTIVE, ON_HOLD, COMPLETED, CANCELLED
	}
}
