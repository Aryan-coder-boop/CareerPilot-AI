package com.careerpilot.auth;

import com.careerpilot.common.security.CustomUserDetailsService;
import com.careerpilot.common.security.JwtTokenProvider;
import com.careerpilot.modules.auth.controller.AuthController;
import com.careerpilot.modules.auth.dto.*;
import com.careerpilot.modules.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Focus on controller layer logic and payload validation
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("POST /api/v1/auth/register should return 201 Created on valid input")
    void testRegisterValidInput() throws Exception {
        RegisterRequest request = new RegisterRequest("Jane Doe", "jane@careerpilot.ai", "Secret123!");
        UserProfileDto profile = new UserProfileDto(UUID.randomUUID(), "jane@careerpilot.ai", "Jane Doe", "ROLE_USER", Instant.now());
        AuthResponse authResponse = new AuthResponse("mock-access", "mock-refresh", 900000L, profile);

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock-access"))
                .andExpect(jsonPath("$.data.user.email").value("jane@careerpilot.ai"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should return 400 Bad Request on invalid email")
    void testRegisterInvalidEmail() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("Jane Doe", "not-an-email", "Secret123!");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.message").value("Validation failed"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login should return 200 OK on valid credentials")
    void testLoginValidInput() throws Exception {
        LoginRequest request = new LoginRequest("jane@careerpilot.ai", "Secret123!");
        UserProfileDto profile = new UserProfileDto(UUID.randomUUID(), "jane@careerpilot.ai", "Jane Doe", "ROLE_USER", Instant.now());
        AuthResponse authResponse = new AuthResponse("mock-access", "mock-refresh", 900000L, profile);

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock-access"))
                .andExpect(jsonPath("$.data.user.fullName").value("Jane Doe"));
    }
}

