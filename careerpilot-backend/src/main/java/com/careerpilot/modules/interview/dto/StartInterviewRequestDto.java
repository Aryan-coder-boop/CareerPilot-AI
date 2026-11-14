package com.careerpilot.modules.interview.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public class StartInterviewRequestDto {

    @NotBlank(message = "Target role is required")
    @Size(min = 2, max = 150, message = "Target role must be between 2 and 150 characters")
    private String targetRole;

    private String difficulty = "MID"; // JUNIOR, MID, SENIOR, LEAD

    private UUID jobId;

    private UUID resumeId;

    @Min(value = 1, message = "Interview must have at least 1 question")
    @Max(value = 10, message = "Maximum 10 questions per mock session")
    private int totalQuestions = 5;

    public StartInterviewRequestDto() {
    }

    public StartInterviewRequestDto(String targetRole, String difficulty, UUID jobId, UUID resumeId, int totalQuestions) {
        this.targetRole = targetRole;
        this.difficulty = difficulty;
        this.jobId = jobId;
        this.resumeId = resumeId;
        this.totalQuestions = totalQuestions;
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

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public UUID getResumeId() {
        return resumeId;
    }

    public void setResumeId(UUID resumeId) {
        this.resumeId = resumeId;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }
}

