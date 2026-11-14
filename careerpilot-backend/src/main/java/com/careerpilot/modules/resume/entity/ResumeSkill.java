package com.careerpilot.modules.resume.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "resume_skills")
public class ResumeSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "skill_name", nullable = false, length = 100)
    private String skillName;

    @Column(name = "category", length = 50)
    private String category = "TECHNICAL";

    @Column(name = "proficiency", length = 30)
    private String proficiency = "UNSPECIFIED";

    public ResumeSkill() {
    }

    public ResumeSkill(Resume resume, String skillName, String category, String proficiency) {
        this.resume = resume;
        this.skillName = skillName;
        this.category = category != null ? category : "TECHNICAL";
        this.proficiency = proficiency != null ? proficiency : "UNSPECIFIED";
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Resume getResume() {
        return resume;
    }

    public void setResume(Resume resume) {
        this.resume = resume;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getProficiency() {
        return proficiency;
    }

    public void setProficiency(String proficiency) {
        this.proficiency = proficiency;
    }
}

