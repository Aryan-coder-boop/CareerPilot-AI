package com.careerpilot.jobmatch;

import com.careerpilot.common.security.CustomUserDetailsService;
import com.careerpilot.common.security.JwtTokenProvider;
import com.careerpilot.common.security.UserPrincipal;
import com.careerpilot.modules.jobmatch.controller.JobMatchController;
import com.careerpilot.modules.jobmatch.dto.JobMatchReportDto;
import com.careerpilot.modules.jobmatch.dto.JobMatchRequestDto;
import com.careerpilot.modules.jobmatch.dto.JobMatchSummaryDto;
import com.careerpilot.modules.jobmatch.service.JobMatchService;
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

@WebMvcTest(JobMatchController.class)
@AutoConfigureMockMvc(addFilters = false)
class JobMatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JobMatchService jobMatchService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private UserPrincipal testPrincipal;
    private final UUID userId = UUID.randomUUID();
    private final UUID resumeId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        testPrincipal = new UserPrincipal(
                userId,
                "test@careerpilot.ai",
                "secret",
                "Test Developer",
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
    @DisplayName("POST /api/v1/jobs/match should return 201 Created on valid request")
    void testMatchJobValid() throws Exception {
        JobMatchRequestDto request = new JobMatchRequestDto(
                resumeId,
                "Staff Software Engineer",
                "TechCorp",
                "Extensive experience in distributed systems, microservices, and event-driven architecture."
        );

        UUID reportId = UUID.randomUUID();
        JobMatchReportDto reportDto = new JobMatchReportDto(
                reportId, resumeId, "resume.pdf", UUID.randomUUID(),
                request.getJobTitle(), request.getCompanyName(),
                88, 92, 82, "High candidate potential",
                List.of("Java", "Spring Boot"), Collections.emptyList(),
                List.of("Ready for interview"), Instant.now()
        );

        when(jobMatchService.matchJobWithResume(eq(userId), any(JobMatchRequestDto.class))).thenReturn(reportDto);

        mockMvc.perform(post("/api/v1/jobs/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.matchScore").value(88))
                .andExpect(jsonPath("$.data.jobTitle").value("Staff Software Engineer"))
                .andExpect(jsonPath("$.data.matchedSkills[0]").value("Java"));
    }

    @Test
    @DisplayName("POST /api/v1/jobs/match should return 400 when job description is too short")
    void testMatchJobInvalidShortDescription() throws Exception {
        JobMatchRequestDto invalidRequest = new JobMatchRequestDto(
                resumeId, "Title", "Company", "Too short"
        );

        mockMvc.perform(post("/api/v1/jobs/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/jobs/matches should return list of user matches")
    void testListUserMatches() throws Exception {
        JobMatchSummaryDto summary = new JobMatchSummaryDto(
                UUID.randomUUID(), UUID.randomUUID(), "Cloud Architect", "AWS",
                resumeId, "resume.pdf", 91, Instant.now()
        );

        when(jobMatchService.getMatchesByUser(userId)).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/v1/jobs/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].jobTitle").value("Cloud Architect"))
                .andExpect(jsonPath("$.data[0].matchScore").value(91));
    }

    @Test
    @DisplayName("GET /api/v1/jobs/matches/{id} should return match report details")
    void testGetMatchReportDetails() throws Exception {
        UUID reportId = UUID.randomUUID();
        JobMatchReportDto reportDto = new JobMatchReportDto(
                reportId, resumeId, "resume.pdf", UUID.randomUUID(),
                "Lead Dev", "Stripe", 89, 90, 88,
                "Strong match", List.of("Java"), Collections.emptyList(),
                Collections.emptyList(), Instant.now()
        );

        when(jobMatchService.getMatchReportById(reportId, userId)).thenReturn(reportDto);

        mockMvc.perform(get("/api/v1/jobs/matches/" + reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(reportId.toString()))
                .andExpect(jsonPath("$.data.jobTitle").value("Lead Dev"));
    }
}

