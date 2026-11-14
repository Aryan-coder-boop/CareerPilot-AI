package com.careerpilot.modules.roadmap.dto;

import java.util.List;
import java.util.UUID;

public class RoadmapMilestoneDto {

    private UUID id;
    private int weekNumber;
    private String title;
    private String description;
    private String status;
    private int orderIndex;
    private List<RoadmapResourceDto> resources;

    public RoadmapMilestoneDto() {
    }

    public RoadmapMilestoneDto(UUID id, int weekNumber, String title, String description,
                               String status, int orderIndex, List<RoadmapResourceDto> resources) {
        this.id = id;
        this.weekNumber = weekNumber;
        this.title = title;
        this.description = description;
        this.status = status;
        this.orderIndex = orderIndex;
        this.resources = resources;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(int weekNumber) {
        this.weekNumber = weekNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public List<RoadmapResourceDto> getResources() {
        return resources;
    }

    public void setResources(List<RoadmapResourceDto> resources) {
        this.resources = resources;
    }
}

