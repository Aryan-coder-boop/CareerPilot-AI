package com.careerpilot.modules.jobmatch.dto;

import java.time.Instant;
import java.util.UUID;

public class JobMatchSummaryDto {

    private UUID id;
    private UUID jobId;
    private String jobTitle;
    private String companyName;
    private UUID resumeId;
    private String resumeFileName;
    private int matchScore;
    private Instant createdAt;

    public JobMatchSummaryDto() {
    }

    public JobMatchSummaryDto(UUID id, UUID jobId, String jobTitle, String companyName,
                              UUID resumeId, String resumeFileName, int matchScore, Instant createdAt) {
        this.id = id;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.companyName = companyName;
        this.resumeId = resumeId;
        this.resumeFileName = resumeFileName;
        this.matchScore = matchScore;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
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

    public UUID getResumeId() {
        return resumeId;
    }

    public void setResumeId(UUID resumeId) {
        this.resumeId = resumeId;
    }

    public String getResumeFileName() {
        return resumeFileName;
    }

    public void setResumeFileName(String resumeFileName) {
        this.resumeFileName = resumeFileName;
    }

    public int getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(int matchScore) {
        this.matchScore = matchScore;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

