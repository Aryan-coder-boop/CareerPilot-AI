package com.careerpilot.auth;

import com.careerpilot.common.exception.BadRequestException;
import com.careerpilot.common.security.JwtTokenProvider;
import com.careerpilot.modules.auth.dto.AuthResponse;
import com.careerpilot.modules.auth.dto.RegisterRequest;
import com.careerpilot.modules.auth.entity.RefreshToken;
import com.careerpilot.modules.auth.entity.Role;
import com.careerpilot.modules.auth.entity.User;
import com.careerpilot.modules.auth.repository.RefreshTokenRepository;
import com.careerpilot.modules.auth.repository.UserRepository;
import com.careerpilot.modules.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("developer@careerpilot.ai", "encodedPassword", "Career Developer", Role.ROLE_USER);
        sampleUser.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Should successfully register a new user")
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest("Career Developer", "developer@careerpilot.ai", "Password123!");

        when(userRepository.existsByEmail("developer@careerpilot.ai")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(tokenProvider.generateTokenFromUser(any(), any(), any(), any())).thenReturn("mock.access.token");
        when(tokenProvider.getRefreshExpirationMs()).thenReturn(604800000L);
        when(tokenProvider.getExpirationMs()).thenReturn(900000L);

        RefreshToken mockRefreshToken = new RefreshToken(sampleUser, "mock-refresh-token", Instant.now().plusSeconds(3600));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(mockRefreshToken);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock.access.token", response.accessToken());
        assertEquals("mock-refresh-token", response.refreshToken());
        assertEquals("developer@careerpilot.ai", response.user().email());
        assertEquals("Career Developer", response.user().fullName());

        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should reject registration when email is already registered")
    void testRegisterDuplicateEmail() {
        RegisterRequest request = new RegisterRequest("Duplicate User", "developer@careerpilot.ai", "Password123!");

        when(userRepository.existsByEmail("developer@careerpilot.ai")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }
}

