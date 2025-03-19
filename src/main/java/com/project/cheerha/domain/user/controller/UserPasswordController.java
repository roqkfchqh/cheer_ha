package com.project.cheerha.domain.user.controller;

import com.project.cheerha.common.dto.ApiResponseDto;
import com.project.cheerha.domain.user.dto.request.ResetPasswordRequestDto;
import com.project.cheerha.domain.user.dto.request.UpdatePasswordRequestDto;
import com.project.cheerha.domain.user.dto.response.UpdatePasswordResponseDto;
import com.project.cheerha.domain.user.service.UserPasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/password")
@RequiredArgsConstructor
public class UserPasswordController {

    private final UserPasswordService userPasswordService;

    @PatchMapping
    public ResponseEntity<ApiResponseDto<UpdatePasswordResponseDto>> updatePassword(
            @AuthenticationPrincipal User userDetails,
            @RequestBody UpdatePasswordRequestDto requestDto
    ) {
        Long userId = Long.valueOf(userDetails.getUsername());
        UpdatePasswordResponseDto responseDto = userPasswordService.updatePassword(userId, requestDto);
        return ApiResponseDto.success(responseDto);
    }

    @PatchMapping("/reset")
    public ResponseEntity<ApiResponseDto<UpdatePasswordResponseDto>> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDto requestDto
    ) {
        UpdatePasswordResponseDto responseDto = userPasswordService.resetPassword(requestDto);
        return ApiResponseDto.success(responseDto);
    }
}

