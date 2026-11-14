package com.careerpilot.modules.jobmatch.controller;

import com.careerpilot.common.exception.UnauthorizedException;
import com.careerpilot.common.payload.ApiResponse;
import com.careerpilot.common.security.UserPrincipal;
import com.careerpilot.modules.jobmatch.dto.JobMatchReportDto;
import com.careerpilot.modules.jobmatch.dto.JobMatchRequestDto;
import com.careerpilot.modules.jobmatch.dto.JobMatchSummaryDto;
import com.careerpilot.modules.jobmatch.service.JobMatchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobMatchController {

    private final JobMatchService jobMatchService;

    public JobMatchController(JobMatchService jobMatchService) {
        this.jobMatchService = jobMatchService;
    }

    @PostMapping("/match")
    public ResponseEntity<ApiResponse<JobMatchReportDto>> matchJobWithResume(
            @Valid @RequestBody JobMatchRequestDto requestDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        validateAuthenticated(userPrincipal);
        JobMatchReportDto report = jobMatchService.matchJobWithResume(userPrincipal.getId(), requestDto);
        return new ResponseEntity<>(
                ApiResponse.success("Job description match report generated successfully", report),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/matches")
    public ResponseEntity<ApiResponse<List<JobMatchSummaryDto>>> listUserMatches(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        validateAuthenticated(userPrincipal);
        List<JobMatchSummaryDto> reports = jobMatchService.getMatchesByUser(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Job match reports retrieved successfully", reports));
    }

    @GetMapping("/matches/{id}")
    public ResponseEntity<ApiResponse<JobMatchReportDto>> getMatchReport(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        validateAuthenticated(userPrincipal);
        JobMatchReportDto report = jobMatchService.getMatchReportById(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Job match report retrieved successfully", report));
    }

    @GetMapping("/matches/by-resume/{resumeId}")
    public ResponseEntity<ApiResponse<List<JobMatchSummaryDto>>> getMatchesByResume(
            @PathVariable("resumeId") UUID resumeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        validateAuthenticated(userPrincipal);
        List<JobMatchSummaryDto> reports = jobMatchService.getMatchesByResume(resumeId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Resume job matches retrieved successfully", reports));
    }

    private void validateAuthenticated(UserPrincipal userPrincipal) {
        if (userPrincipal == null || userPrincipal.getId() == null) {
            throw new UnauthorizedException("Authentication required to access job matching features");
        }
    }
}

