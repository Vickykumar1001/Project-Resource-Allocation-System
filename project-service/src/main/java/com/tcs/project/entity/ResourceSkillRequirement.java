package com.tcs.project.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class ResourceSkillRequirement {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String skillName;
	private Integer requiredCount;

	@Enumerated(EnumType.STRING)
	private MinProficiency minProficiency;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "resource_request_id")
	private ResourceRequest resourceRequest;

	public enum MinProficiency {
		BEGINNER, INTERMEDIATE, EXPERT
	}
}
