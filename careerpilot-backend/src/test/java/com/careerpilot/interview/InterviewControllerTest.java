package com.careerpilot.interview;

import com.careerpilot.common.security.CustomUserDetailsService;
import com.careerpilot.common.security.JwtTokenProvider;
import com.careerpilot.common.security.UserPrincipal;
import com.careerpilot.modules.interview.controller.InterviewController;
import com.careerpilot.modules.interview.dto.*;
import com.careerpilot.modules.interview.service.InterviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InterviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class InterviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InterviewService interviewService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private UserPrincipal testPrincipal;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        testPrincipal = new UserPrincipal(
                userId,
                "test@careerpilot.ai",
                "secret",
                "Mock Candidate",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(testPrincipal, null, testPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /api/v1/interviews/start should return 201 Created")
    void testStartInterviewSuccess() throws Exception {
        StartInterviewRequestDto request = new StartInterviewRequestDto("Java Engineer", "MID", null, null, 3);
        UUID sessionId = UUID.randomUUID();

        InterviewSessionDetailDto sessionDto = new InterviewSessionDetailDto(
                sessionId, "Java Engineer", "MID", "IN_PROGRESS", 3, null, null,
                Instant.now(), null, Collections.emptyList()
        );

        when(interviewService.startSession(eq(userId), any(StartInterviewRequestDto.class))).thenReturn(sessionDto);

        mockMvc.perform(post("/api/v1/interviews/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.targetRole").value("Java Engineer"))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("POST /api/v1/interviews/{sessionId}/submit-answer should return 200 OK")
    void testSubmitAnswerSuccess() throws Exception {
        UUID sessionId = UUID.randomUUID();
        AnswerSubmissionDto submission = new AnswerSubmissionDto(1, "Polymorphism allows objects to take multiple forms.");

        EvaluationResponseDto evalDto = new EvaluationResponseDto(
                1, 90, 92, 86, "Spot on definition", "Model answer", false, 2, null, null
        );

        when(interviewService.submitAnswer(eq(sessionId), eq(userId), any(AnswerSubmissionDto.class))).thenReturn(evalDto);

        mockMvc.perform(post("/api/v1/interviews/" + sessionId + "/submit-answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submission)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.score").value(90))
                .andExpect(jsonPath("$.data.feedback").value("Spot on definition"));
    }

    @Test
    @DisplayName("GET /api/v1/interviews should return user sessions")
    void testListSessions() throws Exception {
        InterviewSessionSummaryDto summary = new InterviewSessionSummaryDto(
                UUID.randomUUID(), "DevOps Engineer", "SENIOR", "COMPLETED", 5, 5, 88,
                Instant.now(), Instant.now()
        );

        when(interviewService.getUserSessions(userId)).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/v1/interviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].targetRole").value("DevOps Engineer"))
                .andExpect(jsonPath("$.data[0].overallScore").value(88));
    }
}

