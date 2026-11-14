package com.careerpilot.modules.resume.controller;

import com.careerpilot.common.exception.UnauthorizedException;
import com.careerpilot.common.payload.ApiResponse;
import com.careerpilot.common.security.UserPrincipal;
import com.careerpilot.modules.resume.dto.ResumeDetailResponseDto;
import com.careerpilot.modules.resume.dto.ResumeSummaryDto;
import com.careerpilot.modules.resume.service.ResumeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ResumeDetailResponseDto>> uploadResume(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        validateAuthenticated(userPrincipal);
        ResumeDetailResponseDto response = resumeService.uploadAndAnalyzeResume(file, userPrincipal.getId());
        return new ResponseEntity<>(
                ApiResponse.success("Resume uploaded and analyzed successfully", response),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ResumeSummaryDto>>> listUserResumes(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        validateAuthenticated(userPrincipal);
        List<ResumeSummaryDto> resumes = resumeService.getUserResumes(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Resumes retrieved successfully", resumes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ResumeDetailResponseDto>> getResumeDetails(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        validateAuthenticated(userPrincipal);
        ResumeDetailResponseDto details = resumeService.getResumeDetail(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Resume details retrieved successfully", details));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteResume(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        validateAuthenticated(userPrincipal);
        resumeService.deleteResume(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Resume deleted successfully", null));
    }

    private void validateAuthenticated(UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            throw new UnauthorizedException("Full authentication is required to access resume services");
        }
    }
}
