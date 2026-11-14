package com.careerpilot.resume;

import com.careerpilot.common.security.CustomUserDetailsService;
import com.careerpilot.common.security.JwtTokenProvider;
import com.careerpilot.common.security.UserPrincipal;
import com.careerpilot.modules.resume.controller.ResumeController;
import com.careerpilot.modules.resume.dto.ParsedResumeAnalysisDto;
import com.careerpilot.modules.resume.dto.ResumeDetailResponseDto;
import com.careerpilot.modules.resume.dto.ResumeSummaryDto;
import com.careerpilot.modules.resume.dto.ScoreBreakdownDto;
import com.careerpilot.modules.resume.service.ResumeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResumeController.class)
@AutoConfigureMockMvc(addFilters = false)
class ResumeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResumeService resumeService;

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
    @DisplayName("GET /api/v1/resumes should return list of user resumes")
    void testListResumes() throws Exception {
        UUID resumeId = UUID.randomUUID();
        ResumeSummaryDto summary = new ResumeSummaryDto(resumeId, "resume.pdf", 85, "PROCESSED", Instant.now());

        when(resumeService.getUserResumes(eq(userId))).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/v1/resumes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].fileName").value("resume.pdf"))
                .andExpect(jsonPath("$.data[0].qualityScore").value(85));
    }

    @Test
    @DisplayName("GET /api/v1/resumes/{id} should return resume details")
    void testGetResumeDetails() throws Exception {
        UUID resumeId = UUID.randomUUID();
        ParsedResumeAnalysisDto analysis = new ParsedResumeAnalysisDto(
                "Jane Doe", "jane@careerpilot.ai", "555-0199", "Seattle", "Summary",
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                new ScoreBreakdownDto(20, 20, 20, 20, 80), 80, List.of("Strong stack"), List.of(),
                ParsedResumeAnalysisDto.DEFAULT_DISCLAIMER
        );

        ResumeDetailResponseDto detail = new ResumeDetailResponseDto(
                resumeId, "resume.pdf", 80, "PROCESSED", Instant.now(), analysis, "preview"
        );

        when(resumeService.getResumeDetail(eq(resumeId), eq(userId))).thenReturn(detail);

        mockMvc.perform(get("/api/v1/resumes/" + resumeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.qualityScore").value(80))
                .andExpect(jsonPath("$.data.analysis.candidateName").value("Jane Doe"));
    }

    @Test
    @DisplayName("DELETE /api/v1/resumes/{id} should delete resume and return 200")
    void testDeleteResume() throws Exception {
        UUID resumeId = UUID.randomUUID();
        doNothing().when(resumeService).deleteResume(eq(resumeId), eq(userId));

        mockMvc.perform(delete("/api/v1/resumes/" + resumeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Resume deleted successfully"));
    }
}
