package com.careerpilot.modules.roadmap.entity;

import com.careerpilot.modules.auth.entity.User;
import com.careerpilot.modules.jobmatch.entity.JobDescription;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "learning_roadmaps")
public class LearningRoadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private JobDescription jobDescription;

    @Column(name = "target_role", nullable = false, length = 150)
    private String targetRole;

    @Column(name = "estimated_weeks", nullable = false)
    private int estimatedWeeks;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "ACTIVE"; // ACTIVE, COMPLETED, ARCHIVED

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "roadmap", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("weekNumber ASC, orderIndex ASC")
    private List<RoadmapMilestone> milestones = new ArrayList<>();

    public LearningRoadmap() {
    }

    public LearningRoadmap(User user, JobDescription jobDescription, String targetRole, int estimatedWeeks) {
        this.user = user;
        this.jobDescription = jobDescription;
        this.targetRole = targetRole;
        this.estimatedWeeks = estimatedWeeks > 0 ? estimatedWeeks : 6;
        this.status = "ACTIVE";
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public JobDescription getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(JobDescription jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public int getEstimatedWeeks() {
        return estimatedWeeks;
    }

    public void setEstimatedWeeks(int estimatedWeeks) {
        this.estimatedWeeks = estimatedWeeks;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<RoadmapMilestone> getMilestones() {
        return milestones;
    }

    public void setMilestones(List<RoadmapMilestone> milestones) {
        this.milestones = milestones;
    }

    public void addMilestone(RoadmapMilestone milestone) {
        milestones.add(milestone);
        milestone.setRoadmap(this);
    }
}

