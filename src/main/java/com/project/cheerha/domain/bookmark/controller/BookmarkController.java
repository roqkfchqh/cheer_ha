package com.project.cheerha.domain.bookmark.controller;

import com.project.cheerha.common.dto.ApiResponseDto;
import com.project.cheerha.domain.bookmark.dto.response.BookmarkCustomAgeResponseDto;
import com.project.cheerha.domain.bookmark.dto.response.ReadBookmarkResponseDto;
import com.project.cheerha.domain.bookmark.service.BookmarkService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/bookmarks")
@RestController
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /**
     * 새로운 북마크를 생성하는 엔드포인트입니다.
     *
     * 요청 본문으로 `CreateBookmarkRequestDto`를 받아서 북마크할 채용 공고 ID를 전달하고,
     * 인증된 사용자의 ID를 받아 북마크를 생성합니다.
     * 서비스 계층에서 북마크를 생성한 후 성공 메시지를 반환합니다.
     *
     * @param jobOpeningId 채용 공고 ID
     * @param userDetails 인증된 사용자의 정보
     * @return 북마크 생성 완료 메시지와 함께 응답
     */
    @PostMapping
    public ResponseEntity<ApiResponseDto<String>> createBookmark(
            @RequestParam Long jobOpeningId,
            @AuthenticationPrincipal User userDetails
    ) {
        Long userId = Long.valueOf(userDetails.getUsername());
        bookmarkService.createBookmark(userId, jobOpeningId);
        return ApiResponseDto.created("즐겨찾기 추가 완료");
    }

    /**
     * 사용자의 모든 북마크를 조회하는 엔드포인트입니다.
     *
     * 이 엔드포인트는 페이지 번호와 페이지 크기를 받아 인증된 사용자의 ID를 기반으로 해당 사용자의 북마크 목록을
     * 페이징 처리하여 조회합니다. 사용자가 요청한 페이지 번호와 크기를 `validatePageSize` 메서드를 통해 검증하고,
     * 그에 맞는 `Pageable` 객체를 생성하여 서비스 메서드에 전달합니다. 조회된 북마크 목록은 페이징된 형태로 반환됩니다.
     *
     * @param userDetails 인증된 사용자의 정보 (JWT 토큰 등을 통해 인증된 사용자)
     * @param page 페이지 번호 (기본값: 1, 1보다 작은 값은 1로 처리됨)
     * @param size 페이지 크기 (기본값: 10, 1보다 작은 값은 1로 처리됨)
     * @return 조회된 북마크 목록을 포함한 응답 (페이징된 형태)
     */
    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<ReadBookmarkResponseDto>>> readAllBookmarks(
            @AuthenticationPrincipal User userDetails,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = Long.valueOf(userDetails.getUsername());
        Pageable pageable = validatePageSize(page, size);
        Page<ReadBookmarkResponseDto> responsePage = bookmarkService.readAllBookmarks(userId, pageable);
        return ApiResponseDto.success(responsePage);
    }

    /**
     * 기존의 북마크를 삭제하는 엔드포인트입니다.
     *
     * 요청 본문으로 `DeleteBookmarkRequestDto`를 받아 삭제할 채용 공고 ID를 전달하고,
     * 인증된 사용자의 ID를 받아 해당 북마크를 삭제합니다.
     * 삭제 후, 삭제 성공 응답을 반환합니다.
     *
     * @param jobOpeningId 삭제할 채용 공고 ID
     * @param userDetails 인증된 사용자의 정보
     * @return 북마크 삭제 성공 응답
     */
    @DeleteMapping
    public ResponseEntity<ApiResponseDto<Void>> deleteBookmark(
            @RequestParam Long jobOpeningId,
            @AuthenticationPrincipal User userDetails
    ) {
        Long userId = Long.valueOf(userDetails.getUsername());
        bookmarkService.deleteBookmark(userId, jobOpeningId);
        return ApiResponseDto.noContent();
    }

    /**
     *  최소, 최대 연령대를 사용자가 커스터마이징하여 추가된 즐겨찾기를 상위 10개를 조회할 수 있는 로직입니다.
     * @param minAge 최소연령
     * @param maxAge 최대연령
     * @return 커스텀 된 즐겨찾기 상위 10개를 List로 반환
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponseDto<List<BookmarkCustomAgeResponseDto>>> readTop10BookmarkByAgeBetween(
        @RequestParam(value = "minAge") int minAge,
        @RequestParam(value = "maxAge") int maxAge
    ) {
        List<BookmarkCustomAgeResponseDto> dtoList = bookmarkService.readTop10BookmarkByAgeBetween(minAge, maxAge);
        return ApiResponseDto.success(dtoList);
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
