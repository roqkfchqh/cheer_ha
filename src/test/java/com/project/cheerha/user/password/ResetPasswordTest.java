package com.project.cheerha.user.password;

import com.project.cheerha.common.exception.client.BadRequestException;
import com.project.cheerha.common.exception.client.ClientErrorCode;
import com.project.cheerha.common.exception.data.NotFoundException;
import com.project.cheerha.domain.user.service.EmailTokenService;
import com.project.cheerha.domain.auth.repository.BannedEmailRepository;
import com.project.cheerha.domain.user.dto.request.ResetPasswordRequestDto;
import com.project.cheerha.domain.user.dto.response.UpdatePasswordResponseDto;
import com.project.cheerha.domain.user.entity.User;
import com.project.cheerha.domain.user.repository.UserRepository;
import com.project.cheerha.domain.user.service.UserFindByService;
import com.project.cheerha.domain.user.service.UserPasswordService;
import com.project.cheerha.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class ResetPasswordTest {

    @InjectMocks
    private UserPasswordService userPasswordService;

    @Mock
    private UserFindByService userFindByService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BannedEmailRepository bannedEmailRepository;

    @Mock
    private EmailTokenService emailTokenService;

    private final String newPassword = "newPassword123";
    private final String encodedPassword = "encodedNewPassword123";
    private final String email = "test@example.com";
    private final String validToken = "validToken";

    @Test
    void resetPassword_성공_이메일밴_해제() {
        // Given
        ResetPasswordRequestDto requestDto = new ResetPasswordRequestDto(email, newPassword, validToken);

        User mockUser = TestUtils.createEntity(User.class, Map.of(
                "email", email,
                "password", encodedPassword
        ));

        when(userRepository.existsByEmail(email)).thenReturn(true);
        doNothing().when(emailTokenService).verifyPasswordResetToken(email, validToken);
        when(userFindByService.findByEmail(email)).thenReturn(mockUser);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(bannedEmailRepository.existsByEmail(email)).thenReturn(true);

        UpdatePasswordResponseDto response = userPasswordService.resetPassword(requestDto);

        assertNotNull(response);
        verify(userRepository, times(1)).existsByEmail(email);
        verify(userFindByService, times(1)).findByEmail(email);
        verify(emailTokenService, times(1)).verifyPasswordResetToken(email, validToken);
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(bannedEmailRepository, times(1)).deleteByEmail(email);
    }

    @Test
    void resetPassword_존재하지_않는_이메일_예외발생() {
        ResetPasswordRequestDto requestDto = new ResetPasswordRequestDto(email, validToken, newPassword);

        when(userRepository.existsByEmail(email)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                userPasswordService.resetPassword(requestDto));

        assertEquals("존재하지 않는 사용자입니다.", exception.getMessage());
        verify(emailTokenService, never()).verifyPasswordResetToken(anyString(), anyString());
    }

    @Test
    void resetPassword_유효하지_않은_토큰_예외발생() {
        ResetPasswordRequestDto requestDto = new ResetPasswordRequestDto(email, newPassword, "invalidToken");

        when(userRepository.existsByEmail(email)).thenReturn(true);
        doThrow(new BadRequestException(ClientErrorCode.INVALID_PASSWORD_RESET_TOKEN))
                .when(emailTokenService).verifyPasswordResetToken(email, "invalidToken");

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                userPasswordService.resetPassword(requestDto));

        assertEquals("패스워드 변경용 토큰이 유효하지 않습니다.", exception.getMessage());
        verify(emailTokenService, times(1)).verifyPasswordResetToken(email, "invalidToken");
    }

    @Test
    void resetPassword_밴된_이메일_아니면_삭제안함() {
        User mockUser = TestUtils.createEntity(User.class, Map.of(
                "email", email,
                "password", encodedPassword
        ));
        ResetPasswordRequestDto requestDto = new ResetPasswordRequestDto(email, validToken, newPassword);

        when(userRepository.existsByEmail(email)).thenReturn(true);
        when(userFindByService.findByEmail(email)).thenReturn(mockUser);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(bannedEmailRepository.existsByEmail(email)).thenReturn(false);

        userPasswordService.resetPassword(requestDto);

        verify(bannedEmailRepository, never()).deleteByEmail(email);
    }
}
