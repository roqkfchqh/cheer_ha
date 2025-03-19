package com.project.cheerha.domain.user.controller;

import com.project.cheerha.common.dto.ApiResponseDto;
import com.project.cheerha.domain.user.dto.request.VerifyNotificationTokenRequestDto;
import com.project.cheerha.domain.user.dto.request.VerifyPasswordResetTokenRequestDto;
import com.project.cheerha.domain.user.dto.request.SendPasswordResetEmailVerificationTokenRequestDto;
import com.project.cheerha.domain.user.dto.response.ActivateNotificationResponseDto;
import com.project.cheerha.domain.user.dto.response.VerifyPasswordResetTokenResponseDto;
import com.project.cheerha.domain.user.dto.response.SendEmailVerificationResponseDto;
import com.project.cheerha.domain.user.service.UserEmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/users/email-verification")
@RestController
@RequiredArgsConstructor
public class UserEmailVerificationController {

    private final UserEmailVerificationService userEmailVerificationService;

    @PostMapping("/send-notification-verify")
    public ResponseEntity<ApiResponseDto<SendEmailVerificationResponseDto>> sendNotificationVerifyEmailVerificationToken(
            @AuthenticationPrincipal User userDetails
    ) {
        Long userId = Long.valueOf(userDetails.getUsername());
        SendEmailVerificationResponseDto responseDto = userEmailVerificationService.sendNotificationVerifyEmailVerificationToken(userId);
        return ApiResponseDto.success(responseDto);
    }

    @PostMapping("/notification-verify")
    public ResponseEntity<ApiResponseDto<ActivateNotificationResponseDto>> verifyNotificationToken(
            @RequestBody VerifyNotificationTokenRequestDto requestDto,
            @AuthenticationPrincipal User userDetails
    ) {
        Long userId = Long.valueOf(userDetails.getUsername());
        userEmailVerificationService.verifyNotificationEmailToken(userId, requestDto.token());
        ActivateNotificationResponseDto responseDto = userEmailVerificationService.activateNotification(userId);
        return ApiResponseDto.success(responseDto);
    }

    @PostMapping("/send-password-reset-verify")
    public ResponseEntity<ApiResponseDto<SendEmailVerificationResponseDto>> sendPasswordResetEmailVerificationToken(
            @RequestBody SendPasswordResetEmailVerificationTokenRequestDto requestDto
    ) {
        SendEmailVerificationResponseDto responseDto = userEmailVerificationService.sendPasswordResetEmailVerificationToken(requestDto.email());
        return ApiResponseDto.success(responseDto);
    }

    @PostMapping("/password-reset-verify")
    public ResponseEntity<ApiResponseDto<VerifyPasswordResetTokenResponseDto>> verifyPasswordResetToken(
            @RequestBody VerifyPasswordResetTokenRequestDto requestDto
    ) {
        userEmailVerificationService.verifyPasswordResetEmailToken(requestDto.email(), requestDto.token());
        VerifyPasswordResetTokenResponseDto responseDto = userEmailVerificationService.createPasswordResetToken(requestDto.email());
        return ApiResponseDto.success(responseDto);
    }

}
