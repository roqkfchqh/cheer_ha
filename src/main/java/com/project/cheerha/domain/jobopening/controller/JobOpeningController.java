package com.project.cheerha.domain.jobopening.controller;

import com.project.cheerha.common.dto.ApiResponseDto;
import com.project.cheerha.domain.jobopening.dto.request.ReadJobOpeningRequestDto;
import com.project.cheerha.domain.jobopening.dto.response.ReadJobOpeningResponseDto;
import com.project.cheerha.domain.jobopening.service.JobOpeningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RequestMapping("/job-opening")
@RestController
@RequiredArgsConstructor
public class JobOpeningController {

    private final JobOpeningService jobOpeningService;

    /**
     * 원하는 채용공고에 리다이렉팅 되게 하는 API입니다.
     * @param id 조회할 채용공고 페이지의 식별 id값
     * @return 리다이렉트 된 채용공고 사이트
     */

    @GetMapping("/{id}")
    public RedirectView getRedirectedView(@PathVariable Long id) {
        return new RedirectView(jobOpeningService.getJobOpeningUrl(id));
    }

    /**
     * 채용 공고 목록을 필터링 및 검색하여 조회하는 API입니다.
     * 사용자는 특정 검색어 및 필터 조건을 설정하여 원하는 채용 공고만 조회할 수 있습니다.
     *
     * @param requestDto 필터링 및 검색어를 포함
     * @param page 조회할 페이지 번호
     * @param size 페이지당 조회할 채용 공고 수
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 필터링된 채용 공고 목록 (페이지)
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponseDto<Page<ReadJobOpeningResponseDto>>> readJobOpenings(
            @Valid @ModelAttribute ReadJobOpeningRequestDto requestDto,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User userDetails
    ) {
        Pageable pageable = validatePageSize(page, size);
        Long userId = Long.valueOf(userDetails.getUsername());
        Page<ReadJobOpeningResponseDto> dtoPage = jobOpeningService.readJobOpenings(
                requestDto, userId, pageable
        );
        return ApiResponseDto.success(dtoPage);
    }

    /**
     * 조회수를 기준으로 상위 100개의 인기 채용 공고를 조회하는 API입니다.
     *
     * 이 API는 클라이언트가 조회수를 기준으로 가장 인기 있는 채용 공고 100개를 요청할 수 있게 해줍니다.
     * 클라이언트는 페이지 번호와 페이지 크기를 매개변수로 전달하여, 페이징 처리된 결과를 받을 수 있습니다.
     * 페이지 번호나 크기가 1보다 작은 경우, 기본값으로 각각 1과 10이 적용됩니다.
     *
     * 페이지 번호(`page`)와 페이지 크기(`size`)는 `@RequestParam`을 통해 전달되며,
     * 이를 바탕으로 조회할 데이터 범위가 결정됩니다.
     *
     * @param page 조회할 페이지 번호 (기본값: 1, 1보다 작으면 1로 처리됨)
     * @param size 페이지 당 조회할 데이터 수 (기본값: 10, 1보다 작으면 1로 처리됨)
     * @return 인기 채용 공고 목록을 포함한 페이징 처리된 응답 객체 (API 응답 형식: ApiResponseDto)
     *         - `ApiResponseDto`: 성공적인 응답을 포함하는 API 응답 객체
     *         - `Page<ReadJobOpeningResponseDto>`: 페이지네이션된 인기 채용 공고 목록
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponseDto<Page<ReadJobOpeningResponseDto>>> readTop100PopularJobOpenings(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = validatePageSize(page, size);
        Page<ReadJobOpeningResponseDto> dtoPage = jobOpeningService.readTop100PopularJobOpenings(pageable);
        return ApiResponseDto.success(dtoPage);
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
