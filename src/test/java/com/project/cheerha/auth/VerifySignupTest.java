package com.project.cheerha.auth;

import com.project.cheerha.common.exception.client.BadRequestException;
import com.project.cheerha.common.exception.client.ClientErrorCode;
import com.project.cheerha.domain.user.service.EmailTokenService;
import com.project.cheerha.domain.auth.dto.request.VerifySignupRequestDto;
import com.project.cheerha.domain.auth.dto.response.VerifySignupResponseDto;
import com.project.cheerha.domain.auth.service.AuthService;
import com.project.cheerha.domain.user.entity.User;
import com.project.cheerha.domain.user.repository.UserRepository;
import com.project.cheerha.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static com.project.cheerha.domain.auth.service.AuthService.SIGNUP_TOKEN_PREFIX;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VerifySignupTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private EmailTokenService emailTokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Test
    void verifySignup_인증에_성공하고_유저_저장() {
        // Given
        VerifySignupRequestDto dto = new VerifySignupRequestDto(
                "test@example.com", "password", "testUser", 25, 0, "token"
        );

        String encodedPassword = "encodedPassword123";
        User mockUser = TestUtils.createEntity(User.class, Map.of(
                "email", "test@example.com",
                "password", encodedPassword,
                "name", "testUser",
                "age", 25,
                "career", 0
        ));
        willDoNothing().given(emailTokenService).verifyEmailToken(anyString(), anyString(), anyString());
        when(passwordEncoder.encode(dto.password())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        //When
        VerifySignupResponseDto response = authService.verifySignup(dto);

        //Then
        verify(emailTokenService).verifyEmailToken(SIGNUP_TOKEN_PREFIX, dto.email(), dto.token());
        verify(passwordEncoder).encode(dto.password());
        verify(userRepository).save(any(User.class));
        assertNotNull(response);
    }

    @Test
    void verifySignup_토큰_불일치() {
        //Given
        VerifySignupRequestDto dto = new VerifySignupRequestDto(
                "test@example.com", "password", "testUser", 25, 0, "invalidToken"
        );
        doThrow(new BadRequestException(ClientErrorCode.INVALID_EMAIL_VERIFICATION_TOKEN))
                .when(emailTokenService)
                .verifyEmailToken(anyString(), anyString(), anyString());

        //When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.verifySignup(dto));
        assertEquals("이메일 인증 토큰이 유효하지 않습니다.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}
