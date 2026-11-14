package com.careerpilot.modules.roadmap.dto;

import java.util.UUID;

public class RoadmapResourceDto {

    private UUID id;
    private String title;
    private String url;
    private String resourceType;

    public RoadmapResourceDto() {
    }

    public RoadmapResourceDto(UUID id, String title, String url, String resourceType) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.resourceType = resourceType;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
}

