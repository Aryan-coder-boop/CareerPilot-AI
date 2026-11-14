package com.careerpilot.modules.interview.controller;

import com.careerpilot.common.exception.UnauthorizedException;
import com.careerpilot.common.payload.ApiResponse;
import com.careerpilot.common.security.UserPrincipal;
import com.careerpilot.modules.interview.dto.*;
import com.careerpilot.modules.interview.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/interviews")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<InterviewSessionDetailDto>> startInterview(
            @Valid @RequestBody StartInterviewRequestDto request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        validateAuthenticated(userPrincipal);
        InterviewSessionDetailDto session = interviewService.startSession(userPrincipal.getId(), request);
        return new ResponseEntity<>(
                ApiResponse.success("Mock interview session started successfully", session),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{sessionId}/current-question")
    public ResponseEntity<ApiResponse<QuestionDto>> getCurrentQuestion(
            @PathVariable("sessionId") UUID sessionId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        validateAuthenticated(userPrincipal);
        QuestionDto question = interviewService.getCurrentQuestion(sessionId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Question retrieved", question));
    }

    @PostMapping("/{sessionId}/submit-answer")
    public ResponseEntity<ApiResponse<EvaluationResponseDto>> submitAnswer(
            @PathVariable("sessionId") UUID sessionId,
            @Valid @RequestBody AnswerSubmissionDto submission,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        validateAuthenticated(userPrincipal);
        EvaluationResponseDto evaluation = interviewService.submitAnswer(sessionId, userPrincipal.getId(), submission);
        return ResponseEntity.ok(ApiResponse.success("Answer evaluated successfully", evaluation));
    }

    @PostMapping("/{sessionId}/finish")
    public ResponseEntity<ApiResponse<InterviewSessionDetailDto>> finishSession(
            @PathVariable("sessionId") UUID sessionId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        validateAuthenticated(userPrincipal);
        InterviewSessionDetailDto session = interviewService.finishSession(sessionId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Interview session finalized", session));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InterviewSessionSummaryDto>>> listSessions(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        validateAuthenticated(userPrincipal);
        List<InterviewSessionSummaryDto> sessions = interviewService.getUserSessions(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Interview sessions retrieved", sessions));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<InterviewSessionDetailDto>> getSessionDetail(
            @PathVariable("sessionId") UUID sessionId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        validateAuthenticated(userPrincipal);
        InterviewSessionDetailDto detail = interviewService.getSessionDetail(sessionId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Session details retrieved", detail));
    }

    @GetMapping("/{sessionId}/report")
    public ResponseEntity<ApiResponse<InterviewSessionDetailDto>> getSessionReport(
            @PathVariable("sessionId") UUID sessionId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        validateAuthenticated(userPrincipal);
        InterviewSessionDetailDto detail = interviewService.getSessionDetail(sessionId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Session report retrieved", detail));
    }

    private void validateAuthenticated(UserPrincipal userPrincipal) {
        if (userPrincipal == null || userPrincipal.getId() == null) {
            throw new UnauthorizedException("Authentication required to access interview features");
        }
    }
}

