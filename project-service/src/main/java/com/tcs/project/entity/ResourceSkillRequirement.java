package com.tcs.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resource_skill_requirement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceSkillRequirement {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String skillName;
    private Integer requiredCount;

    @Enumerated(EnumType.STRING)
    private MinProficiency minProficiency; // optional

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_request_id")
    private ResourceRequest resourceRequest;

    public enum MinProficiency { BEGINNER, INTERMEDIATE, EXPERT }
}
