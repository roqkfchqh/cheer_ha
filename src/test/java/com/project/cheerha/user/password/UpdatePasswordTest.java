package com.project.cheerha.user.password;

import com.project.cheerha.common.exception.client.BadRequestException;
import com.project.cheerha.domain.user.dto.request.UpdatePasswordRequestDto;
import com.project.cheerha.domain.user.dto.response.UpdatePasswordResponseDto;
import com.project.cheerha.domain.user.entity.User;
import com.project.cheerha.domain.user.service.UserFindByService;
import com.project.cheerha.domain.user.service.UserPasswordService;
import com.project.cheerha.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpdatePasswordTest {

    @InjectMocks
    private UserPasswordService userPasswordService;

    @Mock
    private UserFindByService userFindByService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final Long userId = 1L;
    private final String oldPassword = "oldPassword123";
    private final String newPassword = "newPassword123";
    private final String encodedPassword = "encodedNewPassword123";
    private final String email = "test@example.com";

    @Test
    void updatePassword_성공() {
        UpdatePasswordRequestDto requestDto = new UpdatePasswordRequestDto(oldPassword, newPassword);
        User mockUser = TestUtils.createEntity(User.class, Map.of(
                "email", email,
                "password", encodedPassword
        ));

        when(userFindByService.findById(userId)).thenReturn(mockUser);
        when(passwordEncoder.matches(oldPassword, mockUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);

        UpdatePasswordResponseDto response = userPasswordService.updatePassword(userId, requestDto);

        assertNotNull(response);
        assertEquals(encodedPassword, mockUser.getPassword());
        verify(passwordEncoder, times(1)).matches(oldPassword, mockUser.getPassword());
        verify(passwordEncoder, times(1)).encode(newPassword);
    }

    @Test
    void updatePassword_현재비밀번호_불일치() {
        UpdatePasswordRequestDto requestDto = new UpdatePasswordRequestDto("wrongPassword", newPassword);
        User mockUser = TestUtils.createEntity(User.class, Map.of(
                "email", email,
                "password", encodedPassword
        ));

        when(userFindByService.findById(userId)).thenReturn(mockUser);
        when(passwordEncoder.matches("wrongPassword", mockUser.getPassword())).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                userPasswordService.updatePassword(userId, requestDto));

        assertEquals("현재 패스워드가 잘못되었습니다.", exception.getMessage());
        verify(passwordEncoder, times(1)).matches("wrongPassword", mockUser.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
    }
}
