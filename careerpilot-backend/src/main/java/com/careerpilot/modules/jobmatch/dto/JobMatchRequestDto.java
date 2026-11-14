package com.careerpilot.modules.jobmatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public class JobMatchRequestDto {

    @NotNull(message = "Resume ID is required")
    private UUID resumeId;

    @NotBlank(message = "Job title is required")
    @Size(min = 2, max = 255, message = "Job title must be between 2 and 255 characters")
    private String jobTitle;

    @Size(max = 255, message = "Company name cannot exceed 255 characters")
    private String companyName;

    @NotBlank(message = "Job description text is required")
    @Size(min = 20, message = "Job description must be at least 20 characters for meaningful analysis")
    private String jobDescriptionText;

    public JobMatchRequestDto() {
    }

    public JobMatchRequestDto(UUID resumeId, String jobTitle, String companyName, String jobDescriptionText) {
        this.resumeId = resumeId;
        this.jobTitle = jobTitle;
        this.companyName = companyName;
        this.jobDescriptionText = jobDescriptionText;
    }

    public UUID getResumeId() {
        return resumeId;
    }

    public void setResumeId(UUID resumeId) {
        this.resumeId = resumeId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobDescriptionText() {
        return jobDescriptionText;
    }

    public void setJobDescriptionText(String jobDescriptionText) {
        this.jobDescriptionText = jobDescriptionText;
    }
}

