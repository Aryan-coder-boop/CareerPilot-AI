package com.careerpilot.roadmap;

import com.careerpilot.common.security.CustomUserDetailsService;
import com.careerpilot.common.security.JwtTokenProvider;
import com.careerpilot.common.security.UserPrincipal;
import com.careerpilot.modules.roadmap.controller.RoadmapController;
import com.careerpilot.modules.roadmap.dto.*;
import com.careerpilot.modules.roadmap.service.RoadmapService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoadmapController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoadmapControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoadmapService roadmapService;

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
                "Learner",
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
    @DisplayName("POST /api/v1/roadmaps/generate should return 201 Created")
    void testGenerateRoadmapSuccess() throws Exception {
        RoadmapGenerateRequestDto request = new RoadmapGenerateRequestDto("Java Engineer", null, null, 6);
        UUID roadmapId = UUID.randomUUID();

        RoadmapDetailDto detail = new RoadmapDetailDto(
                roadmapId, "Java Engineer", 6, "ACTIVE", 0, Instant.now(), Collections.emptyList()
        );

        when(roadmapService.generateRoadmap(eq(userId), any(RoadmapGenerateRequestDto.class))).thenReturn(detail);

        mockMvc.perform(post("/api/v1/roadmaps/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.targetRole").value("Java Engineer"));
    }

    @Test
    @DisplayName("GET /api/v1/roadmaps should return user roadmaps")
    void testListRoadmaps() throws Exception {
        RoadmapSummaryDto summary = new RoadmapSummaryDto(
                UUID.randomUUID(), "Cloud Architect", 8, "ACTIVE", 8, 2, 25, Instant.now()
        );

        when(roadmapService.getUserRoadmaps(userId)).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/v1/roadmaps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].targetRole").value("Cloud Architect"))
                .andExpect(jsonPath("$.data[0].progressPercentage").value(25));
    }

    @Test
    @DisplayName("PATCH /api/v1/roadmaps/milestones/{id} should update milestone status")
    void testUpdateMilestoneStatus() throws Exception {
        UUID milestoneId = UUID.randomUUID();
        RoadmapMilestoneStatusUpdateDto updateDto = new RoadmapMilestoneStatusUpdateDto("COMPLETED");

        RoadmapMilestoneDto dto = new RoadmapMilestoneDto(
                milestoneId, 1, "Week 1", "Tasks", "COMPLETED", 1, Collections.emptyList()
        );

        when(roadmapService.updateMilestoneStatus(eq(milestoneId), eq(userId), eq("COMPLETED"))).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/roadmaps/milestones/" + milestoneId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }
}

