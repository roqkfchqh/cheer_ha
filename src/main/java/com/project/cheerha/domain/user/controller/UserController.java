package com.project.cheerha.domain.user.controller;

import com.project.cheerha.domain.user.dto.response.*;
import com.project.cheerha.domain.user.service.UserService;
import com.project.cheerha.common.dto.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/users")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponseDto<ReadUserResponseDto>> readUser(
            @AuthenticationPrincipal User userDetails
    ) {
        Long userId = Long.valueOf(userDetails.getUsername());
        ReadUserResponseDto responseDto = userService.readUser(userId);
        return ApiResponseDto.success(responseDto);
    }
}
