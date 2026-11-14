package com.careerpilot.modules.roadmap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class RoadmapMilestoneStatusUpdateDto {

    @NotBlank(message = "Status cannot be blank")
    @Pattern(regexp = "^(PENDING|IN_PROGRESS|COMPLETED)$", message = "Status must be PENDING, IN_PROGRESS, or COMPLETED")
    private String status;

    public RoadmapMilestoneStatusUpdateDto() {
    }

    public RoadmapMilestoneStatusUpdateDto(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

