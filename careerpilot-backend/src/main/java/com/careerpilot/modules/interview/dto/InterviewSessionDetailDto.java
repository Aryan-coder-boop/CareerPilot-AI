package com.careerpilot.modules.interview.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class InterviewSessionDetailDto {

    private UUID id;
    private String targetRole;
    private String difficulty;
    private String status;
    private int totalQuestions;
    private Integer overallScore;
    private String overallFeedback;
    private Instant createdAt;
    private Instant completedAt;
    private List<QuestionDto> questions;

    public InterviewSessionDetailDto() {
    }

    public InterviewSessionDetailDto(UUID id, String targetRole, String difficulty, String status,
                                     int totalQuestions, Integer overallScore, String overallFeedback,
                                     Instant createdAt, Instant completedAt, List<QuestionDto> questions) {
        this.id = id;
        this.targetRole = targetRole;
        this.difficulty = difficulty;
        this.status = status;
        this.totalQuestions = totalQuestions;
        this.overallScore = overallScore;
        this.overallFeedback = overallFeedback;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.questions = questions;
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

    public Integer getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Integer overallScore) {
        this.overallScore = overallScore;
    }

    public String getOverallFeedback() {
        return overallFeedback;
    }

    public void setOverallFeedback(String overallFeedback) {
        this.overallFeedback = overallFeedback;
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

    public List<QuestionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionDto> questions) {
        this.questions = questions;
    }
}

