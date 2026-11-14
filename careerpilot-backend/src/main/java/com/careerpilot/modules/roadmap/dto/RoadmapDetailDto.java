package com.careerpilot.modules.roadmap.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class RoadmapDetailDto {

    private UUID id;
    private String targetRole;
    private int estimatedWeeks;
    private String status;
    private int progressPercentage;
    private Instant createdAt;
    private List<RoadmapMilestoneDto> milestones;

    public RoadmapDetailDto() {
    }

    public RoadmapDetailDto(UUID id, String targetRole, int estimatedWeeks, String status,
                            int progressPercentage, Instant createdAt, List<RoadmapMilestoneDto> milestones) {
        this.id = id;
        this.targetRole = targetRole;
        this.estimatedWeeks = estimatedWeeks;
        this.status = status;
        this.progressPercentage = progressPercentage;
        this.createdAt = createdAt;
        this.milestones = milestones;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public int getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(int progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<RoadmapMilestoneDto> getMilestones() {
        return milestones;
    }

    public void setMilestones(List<RoadmapMilestoneDto> milestones) {
        this.milestones = milestones;
    }
}

