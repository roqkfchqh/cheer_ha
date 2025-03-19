package com.project.cheerha.domain.userkeyword.controller;

import com.project.cheerha.common.dto.ApiResponseDto;
import com.project.cheerha.domain.userkeyword.dto.request.CreateUserKeywordRequestDto;
import com.project.cheerha.domain.userkeyword.dto.response.CreateUserKeywordResponseDto;
import com.project.cheerha.domain.userkeyword.dto.response.ReadUserKeywordResponseDto;
import com.project.cheerha.domain.userkeyword.service.UserKeywordService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/users/keywords")
@RestController
@RequiredArgsConstructor
public class UserKeywordController {

    private final UserKeywordService userKeywordService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<CreateUserKeywordResponseDto>> createUserKeywordList(
            @RequestBody CreateUserKeywordRequestDto requestDto,
            @AuthenticationPrincipal User userDetails
    ) {
        Long userId = Long.valueOf(userDetails.getUsername());
        CreateUserKeywordResponseDto responseDto = userKeywordService.createUserKeyword(
                userId,
                requestDto
        );
        return ApiResponseDto.created(responseDto);
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<ReadUserKeywordResponseDto>>> readAllUserKeywords(
            @AuthenticationPrincipal User userDetails
    ) {
        Long userId = Long.valueOf(userDetails.getUsername());
        List<ReadUserKeywordResponseDto> responseDto = userKeywordService.readAllUserKeywords(userId);
        return ApiResponseDto.success(responseDto);
    }

    @DeleteMapping
    public ResponseEntity<ApiResponseDto<Void>> deleteUserKeyword(
        @RequestParam List<Long> userKeywordIdList,
        @AuthenticationPrincipal User userDetails
    ) {
        Long userId = Long.valueOf(userDetails.getUsername());
        userKeywordService.deleteUserKeyword(userId, userKeywordIdList);
        return ApiResponseDto.noContent();
    }
}
