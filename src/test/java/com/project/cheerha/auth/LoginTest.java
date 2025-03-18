package com.project.cheerha.auth;

import com.project.cheerha.common.exception.auth.AuthErrorCode;
import com.project.cheerha.common.exception.auth.UnAuthorizedException;
import com.project.cheerha.domain.auth.service.RefreshTokenService;
import com.project.cheerha.common.util.JwtUtil;
import com.project.cheerha.domain.auth.dto.request.CreateLoginRequestDto;
import com.project.cheerha.domain.auth.dto.response.CreateLoginResponseDto;
import com.project.cheerha.domain.auth.service.AuthService;
import com.project.cheerha.domain.user.entity.User;
import com.project.cheerha.domain.user.service.UserFindByService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoginTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserFindByService userFindByService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void testLogin_로그인_성공했을때() {
        //given
        CreateLoginRequestDto dto = new CreateLoginRequestDto("test@example.com", "password");
        User user = mock(User.class);
        when(userFindByService.findByEmail(dto.email())).thenReturn(user);
        when(passwordEncoder.matches(dto.password(), user.getPassword())).thenReturn(true);
        when(jwtUtil.createToken(anyLong(), any())).thenReturn("accessToken");
        when(jwtUtil.createRefreshToken(anyLong())).thenReturn("refreshToken");
        doNothing().when(refreshTokenService).createRefreshToken(anyLong(), anyString());

        //when
        CreateLoginResponseDto response = authService.login(dto);

        //then
        assertNotNull(response);
        assertEquals("accessToken", response.token());
        assertEquals("refreshToken", response.refreshToken());
    }

    @Test
    void testLogin_비밀번호_틀릴때() {
        //given
        CreateLoginRequestDto dto = new CreateLoginRequestDto("test@example.com", "wrongpassword");
        User user = mock(User.class);
        when(userFindByService.findByEmail(dto.email())).thenReturn(user);
        when(passwordEncoder.matches(dto.password(), user.getPassword())).thenReturn(false);

        //when & then
        UnAuthorizedException exception = assertThrows(UnAuthorizedException.class, () -> authService.login(dto));
        assertEquals(AuthErrorCode.INVALID_PASSWORD.getMessage(), exception.getMessage());
    }
}
