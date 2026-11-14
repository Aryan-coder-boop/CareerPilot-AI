package com.careerpilot.modules.roadmap.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "roadmap_resources")
public class RoadmapResource {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "milestone_id", nullable = false)
    private RoadmapMilestone milestone;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "url", nullable = false, length = 1000)
    private String url;

    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType; // DOCUMENTATION, ARTICLE, VIDEO, REPO

    public RoadmapResource() {
    }

    public RoadmapResource(RoadmapMilestone milestone, String title, String url, String resourceType) {
        this.milestone = milestone;
        this.title = title;
        this.url = url;
        this.resourceType = resourceType != null ? resourceType.toUpperCase() : "DOCUMENTATION";
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public RoadmapMilestone getMilestone() {
        return milestone;
    }

    public void setMilestone(RoadmapMilestone milestone) {
        this.milestone = milestone;
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

