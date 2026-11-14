package com.careerpilot.modules.jobmatch.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class JobMatchReportDto {

    private UUID id;
    private UUID resumeId;
    private String resumeFileName;
    private UUID jobId;
    private String jobTitle;
    private String companyName;
    private int matchScore;
    private int skillsScore;
    private int experienceScore;
    private String matchSummary;
    private List<String> matchedSkills;
    private List<SkillMatchItemDto> missingSkills;
    private List<String> recommendations;
    private Instant createdAt;

    public JobMatchReportDto() {
    }

    public JobMatchReportDto(UUID id, UUID resumeId, String resumeFileName, UUID jobId,
                             String jobTitle, String companyName, int matchScore,
                             int skillsScore, int experienceScore, String matchSummary,
                             List<String> matchedSkills, List<SkillMatchItemDto> missingSkills,
                             List<String> recommendations, Instant createdAt) {
        this.id = id;
        this.resumeId = resumeId;
        this.resumeFileName = resumeFileName;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.companyName = companyName;
        this.matchScore = matchScore;
        this.skillsScore = skillsScore;
        this.experienceScore = experienceScore;
        this.matchSummary = matchSummary;
        this.matchedSkills = matchedSkills;
        this.missingSkills = missingSkills;
        this.recommendations = recommendations;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public int getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(int matchScore) {
        this.matchScore = matchScore;
    }

    public int getSkillsScore() {
        return skillsScore;
    }

    public void setSkillsScore(int skillsScore) {
        this.skillsScore = skillsScore;
    }

    public int getExperienceScore() {
        return experienceScore;
    }

    public void setExperienceScore(int experienceScore) {
        this.experienceScore = experienceScore;
    }

    public String getMatchSummary() {
        return matchSummary;
    }

    public void setMatchSummary(String matchSummary) {
        this.matchSummary = matchSummary;
    }

    public List<String> getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(List<String> matchedSkills) {
        this.matchedSkills = matchedSkills;
    }

    public List<SkillMatchItemDto> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<SkillMatchItemDto> missingSkills) {
        this.missingSkills = missingSkills;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

