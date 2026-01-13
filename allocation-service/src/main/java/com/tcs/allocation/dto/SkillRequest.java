package com.tcs.allocation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillRequest {
	private String name;
	private Proficiency proficiency;

	public static enum Proficiency {
		BEGINNER, INTERMEDIATE, EXPERT
	}

}
