package com.tcs.employee.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "skill", uniqueConstraints = @UniqueConstraint(columnNames = { "name" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Skill {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
}
