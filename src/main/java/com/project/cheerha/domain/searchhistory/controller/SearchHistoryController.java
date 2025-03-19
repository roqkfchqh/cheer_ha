package com.project.cheerha.domain.searchhistory.controller;

import com.project.cheerha.common.dto.ApiResponseDto;
import com.project.cheerha.domain.searchhistory.service.SearchHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/search-history")
@RestController
@RequiredArgsConstructor
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;

    /**
     * 사용자의 최근 검색어 목록을 조회하는 API입니다.
     *
     * Redis에 저장된 최근 검색어 목록을 반환합니다.
     * 첫번째 조회 시 검색어가 없다면, 다시 조회하여 DB에서 검색어 목록을 가져옵니다.
     *
     * @param userDetails 로그인한 유저의 정보
     * @return 최근 검색어 목록 (10개)
     */
    @GetMapping
    public ResponseEntity<ApiResponseDto<List<String>>> readAllHistories(
            @AuthenticationPrincipal User userDetails
    ) {
        Long userId = Long.valueOf(userDetails.getUsername());
        List<String> SearchTermsList = searchHistoryService.getRecentSearchTerms(userId);

        if (SearchTermsList.isEmpty()) {
            SearchTermsList = searchHistoryService.getRecentSearchTerms(userId);
        }

        return ApiResponseDto.success(SearchTermsList);
    }
}
