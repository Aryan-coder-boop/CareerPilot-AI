package com.careerpilot.modules.roadmap.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public class RoadmapGenerateRequestDto {

    @NotBlank(message = "Target role is required")
    @Size(min = 2, max = 150, message = "Target role must be between 2 and 150 characters")
    private String targetRole;

    private UUID jobId;

    private UUID matchReportId;

    @Min(value = 2, message = "Roadmap duration must be at least 2 weeks")
    @Max(value = 16, message = "Roadmap duration cannot exceed 16 weeks")
    private int targetWeeks = 6;

    public RoadmapGenerateRequestDto() {
    }

    public RoadmapGenerateRequestDto(String targetRole, UUID jobId, UUID matchReportId, int targetWeeks) {
        this.targetRole = targetRole;
        this.jobId = jobId;
        this.matchReportId = matchReportId;
        this.targetWeeks = targetWeeks;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public UUID getMatchReportId() {
        return matchReportId;
    }

    public void setMatchReportId(UUID matchReportId) {
        this.matchReportId = matchReportId;
    }

    public int getTargetWeeks() {
        return targetWeeks;
    }

    public void setTargetWeeks(int targetWeeks) {
        this.targetWeeks = targetWeeks;
    }
}

