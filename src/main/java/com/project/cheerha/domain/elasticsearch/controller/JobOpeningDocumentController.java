package com.project.cheerha.domain.elasticsearch.controller;

import com.project.cheerha.common.dto.ElasticApiResponseDto;
import com.project.cheerha.domain.elasticsearch.dto.request.ReadJobOpeningElasticAutoRequestDto;
import com.project.cheerha.domain.elasticsearch.dto.request.ReadJobOpeningElasticRequestDto;
import com.project.cheerha.domain.elasticsearch.dto.response.ReadJobOpeningElasticAutoResponseDto;
import com.project.cheerha.domain.elasticsearch.dto.response.ReadJobOpeningElasticResponseDto;
import com.project.cheerha.domain.elasticsearch.service.JobOpeningDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/job-opening")
@RestController
@RequiredArgsConstructor
public class JobOpeningDocumentController {

    private final JobOpeningDocumentService jobOpeningDocumentService;

    /**
     * Elasticsearch를 사용하여 모든 채용 공고를 조회하는 메서드입니다.
     *
     * Elasticsearch에서 모든 채용 공고를 검색하고, 결과를 DTO로 반환합니다.
     * 페이지네이션을 지원하여, 필요한 페이지 범위의 데이터만을 조회합니다.
     *
     * @param page 조회할 페이지 번호 (기본값: 1)
     * @param size 페이지 당 조회할 데이터 수 (기본값: 10)
     * @return Elasticsearch에서 조회된 채용 공고 목록을 포함한 API 응답 객체
     */
    @GetMapping("/search/elastic")
    public ResponseEntity<ElasticApiResponseDto<Page<ReadJobOpeningElasticResponseDto>>> readJobOpeningsUsingElasticsearch(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = validatePageSize(page, size);
        Page<ReadJobOpeningElasticResponseDto> dtoList = jobOpeningDocumentService.readAllJobOpeningsUsingElasticsearch(pageable);
        int totalItems = (int) dtoList.getTotalElements();
        String message = "채용공고 " + totalItems + "개가 조회되었습니다.";
        return ElasticApiResponseDto.success(dtoList, message);
    }

    /**
     * 조회수 기준으로 인기 채용 공고를 Elasticsearch에서 조회하는 메서드입니다.
     *
     * 이 API는 조회수 기준으로 내림차순 정렬된 인기 채용 공고를 100개까지 조회합니다.
     * 페이지네이션을 지원하며, 페이지 번호와 크기를 매개변수로 전달받습니다.
     *
     * @param page 조회할 페이지 번호 (기본값: 1)
     * @param size 페이지 당 조회할 데이터 수 (기본값: 10)
     * @return Elasticsearch에서 조회된 인기 채용 공고 목록을 포함한 API 응답 객체
     */
    @GetMapping("/popular/elastic")
    public ResponseEntity<ElasticApiResponseDto<Page<ReadJobOpeningElasticResponseDto>>> readTop100PopularJobOpeningsUsingElasticsearch(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = validatePageSize(page, size);
        Page<ReadJobOpeningElasticResponseDto> dtoPage = jobOpeningDocumentService.readTop100PopularJobOpeningsUsingElasticsearch(pageable);
        int totalItems = (int) dtoPage.getTotalElements();
        String message = "채용공고 " + totalItems + "개가 조회되었습니다.";
        return ElasticApiResponseDto.success(dtoPage, message);
    }

    /**
     * 전체 채용 공고 데이터와 필터링 및 검색어를 통해 Elasticsearch에서 데이터를 조회하는 API입니다.
     *
     * @param requestDto 사용자가 입력한 필터링 조건을 포함한 DTO
     * @param userDetails 현재 로그인한 사용자의 정보
     * @param page 조회할 페이지 번호
     * @param size 페이지 당 조회할 데이터 수
     * @return 필터링된 채용 공고 목록을 페이지네이션 형태로 반환
     */
    @GetMapping("/search/elastic/filters")
    public ResponseEntity<ElasticApiResponseDto<Page<ReadJobOpeningElasticResponseDto>>> readJobOpeningElasticsearch(
        @Valid @ModelAttribute ReadJobOpeningElasticRequestDto requestDto,
        @AuthenticationPrincipal User userDetails,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = validatePageSize(page, size);
        Long userId = Long.valueOf(userDetails.getUsername());
        Page<ReadJobOpeningElasticResponseDto> jobOpeningElasticResponseDtoPage = jobOpeningDocumentService.readJobOpeningUsingElasticSearchFilter(requestDto, userId, pageable);
        int totalItems = (int) jobOpeningElasticResponseDtoPage.getTotalElements();
        String message = "채용공고 " + totalItems + "개가 조회되었습니다.";
        return ElasticApiResponseDto.success(jobOpeningElasticResponseDtoPage, message);
    }

    /**
     * Elasticsearch의 자동 완성 검색 기능을 활용하여 데이터를 조회하는 API입니다.
     *
     * @param requestDto 사용자가 입력한 검색어를 포함한 DTO
     * @param userDetails 현재 로그인한 사용자의 정보
     * @param page 조회할 페이지 번호
     * @param size 페이지 당 조회할 데이터 수
     * @return 자동 완성으로 검색된 채용 공고 목록을 페이지네이션 형태로 반환
     */
    @GetMapping("/search/elastic/auto")
    public ResponseEntity<ElasticApiResponseDto<Page<ReadJobOpeningElasticAutoResponseDto>>> readJobOpeningElasticAuto(
        @ModelAttribute ReadJobOpeningElasticAutoRequestDto requestDto,
        @AuthenticationPrincipal User userDetails,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = validatePageSize(page, size);
        Long userId = Long.valueOf(userDetails.getUsername());
        Page<ReadJobOpeningElasticAutoResponseDto> jobOpeningElasticResponseDtoPage = jobOpeningDocumentService.readJobOpeningElasticAuto(requestDto, userId, pageable);
        int totalItems = (int) jobOpeningElasticResponseDtoPage.getTotalElements();
        String message = "채용공고 " + totalItems + "개가 조회되었습니다.";
        return ElasticApiResponseDto.success(jobOpeningElasticResponseDtoPage, message);
    }

    /**
     * 페이지 번호와 페이지 크기를 검증하여 유효한 값으로 반환하는 메서드입니다.
     *
     * 이 메서드는 페이지 번호(page)와 페이지 크기(size)가 유효한지 확인하고,
     * 페이지 번호는 최소 1부터 시작하도록 보정하며,
     * 페이지 크기는 최소 1 이상으로 보정한 후, PageRequest 객체를 반환합니다.
     *
     * @param page 페이지 번호 (1 이상이어야 함)
     * @param size 페이지 크기 (1 이상이어야 함)
     * @return 검증된 페이지 번호와 크기를 반영한 PageRequest 객체
     */
    private Pageable validatePageSize(int page, int size) {
        if (page < 1 || size < 1) {
            page = Math.max(1, page);
            size = Math.max(1, size);
        }
        return PageRequest.of(page - 1, size);
    }
}
