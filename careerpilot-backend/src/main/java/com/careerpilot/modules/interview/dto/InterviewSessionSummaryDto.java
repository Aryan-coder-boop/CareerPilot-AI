package com.careerpilot.modules.interview.dto;

import java.time.Instant;
import java.util.UUID;

public class InterviewSessionSummaryDto {

    private UUID id;
    private String targetRole;
    private String difficulty;
    private String status;
    private int totalQuestions;
    private int completedQuestions;
    private Integer overallScore;
    private Instant createdAt;
    private Instant completedAt;

    public InterviewSessionSummaryDto() {
    }

    public InterviewSessionSummaryDto(UUID id, String targetRole, String difficulty, String status,
                                     int totalQuestions, int completedQuestions, Integer overallScore,
                                     Instant createdAt, Instant completedAt) {
        this.id = id;
        this.targetRole = targetRole;
        this.difficulty = difficulty;
        this.status = status;
        this.totalQuestions = totalQuestions;
        this.completedQuestions = completedQuestions;
        this.overallScore = overallScore;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
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

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getCompletedQuestions() {
        return completedQuestions;
    }

    public void setCompletedQuestions(int completedQuestions) {
        this.completedQuestions = completedQuestions;
    }

    public Integer getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Integer overallScore) {
        this.overallScore = overallScore;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}

