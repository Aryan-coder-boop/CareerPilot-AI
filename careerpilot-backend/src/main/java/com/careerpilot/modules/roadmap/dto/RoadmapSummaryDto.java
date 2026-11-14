package com.careerpilot.modules.roadmap.dto;

import java.time.Instant;
import java.util.UUID;

public class RoadmapSummaryDto {

    private UUID id;
    private String targetRole;
    private int estimatedWeeks;
    private String status;
    private int totalMilestones;
    private int completedMilestones;
    private int progressPercentage;
    private Instant createdAt;

    public RoadmapSummaryDto() {
    }

    public RoadmapSummaryDto(UUID id, String targetRole, int estimatedWeeks, String status,
                             int totalMilestones, int completedMilestones, int progressPercentage,
                             Instant createdAt) {
        this.id = id;
        this.targetRole = targetRole;
        this.estimatedWeeks = estimatedWeeks;
        this.status = status;
        this.totalMilestones = totalMilestones;
        this.completedMilestones = completedMilestones;
        this.progressPercentage = progressPercentage;
        this.createdAt = createdAt;
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

    public int getTotalMilestones() {
        return totalMilestones;
    }

    public void setTotalMilestones(int totalMilestones) {
        this.totalMilestones = totalMilestones;
    }

    public int getCompletedMilestones() {
        return completedMilestones;
    }

    public void setCompletedMilestones(int completedMilestones) {
        this.completedMilestones = completedMilestones;
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
}

