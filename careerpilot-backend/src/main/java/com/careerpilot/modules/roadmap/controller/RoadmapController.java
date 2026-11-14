package com.careerpilot.modules.roadmap.controller;

import com.careerpilot.common.exception.UnauthorizedException;
import com.careerpilot.common.payload.ApiResponse;
import com.careerpilot.common.security.UserPrincipal;
import com.careerpilot.modules.roadmap.dto.*;
import com.careerpilot.modules.roadmap.service.RoadmapService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roadmaps")
public class RoadmapController {

    private final RoadmapService roadmapService;

    public RoadmapController(RoadmapService roadmapService) {
        this.roadmapService = roadmapService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<RoadmapDetailDto>> generateRoadmap(
            @Valid @RequestBody RoadmapGenerateRequestDto request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        validateAuthenticated(userPrincipal);
        RoadmapDetailDto roadmap = roadmapService.generateRoadmap(userPrincipal.getId(), request);
        return new ResponseEntity<>(
                ApiResponse.success("Learning roadmap generated successfully", roadmap),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoadmapSummaryDto>>> listRoadmaps(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        validateAuthenticated(userPrincipal);
        List<RoadmapSummaryDto> list = roadmapService.getUserRoadmaps(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("User roadmaps retrieved", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoadmapDetailDto>> getRoadmapDetail(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        validateAuthenticated(userPrincipal);
        RoadmapDetailDto detail = roadmapService.getRoadmapDetail(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Roadmap detail retrieved", detail));
    }

    @PatchMapping("/milestones/{milestoneId}")
    public ResponseEntity<ApiResponse<RoadmapMilestoneDto>> updateMilestoneStatus(
            @PathVariable("milestoneId") UUID milestoneId,
            @Valid @RequestBody RoadmapMilestoneStatusUpdateDto statusDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        validateAuthenticated(userPrincipal);
        RoadmapMilestoneDto updated = roadmapService.updateMilestoneStatus(milestoneId, userPrincipal.getId(), statusDto.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Milestone status updated", updated));
    }

    private void validateAuthenticated(UserPrincipal userPrincipal) {
        if (userPrincipal == null || userPrincipal.getId() == null) {
            throw new UnauthorizedException("Authentication required to access learning roadmaps");
        }
    }
}

