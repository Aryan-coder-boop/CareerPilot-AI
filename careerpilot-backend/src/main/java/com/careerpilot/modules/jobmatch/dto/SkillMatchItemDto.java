package com.careerpilot.modules.jobmatch.dto;

public class SkillMatchItemDto {

    private String skillName;
    private String category;
    private String importance; // "REQUIRED" or "PREFERRED"
    private String status;     // "MATCHED" or "MISSING"
    private String recommendation;

    public SkillMatchItemDto() {
    }

    public SkillMatchItemDto(String skillName, String category, String importance, String status, String recommendation) {
        this.skillName = skillName;
        this.category = category;
        this.importance = importance;
        this.status = status;
        this.recommendation = recommendation;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImportance() {
        return importance;
    }

    public void setImportance(String importance) {
        this.importance = importance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
}

