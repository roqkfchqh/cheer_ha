package com.project.cheerha.domain.bookmark.service;

import com.project.cheerha.common.exception.client.BadRequestException;
import com.project.cheerha.common.exception.client.ClientErrorCode;
import com.project.cheerha.domain.bookmark.dto.response.BookmarkCustomAgeResponseDto;
import com.project.cheerha.domain.bookmark.dto.response.ReadBookmarkResponseDto;
import com.project.cheerha.domain.bookmark.entity.Bookmark;
import com.project.cheerha.domain.bookmark.repository.BookmarkRepository;
import com.project.cheerha.domain.jobopening.entity.JobOpening;
import com.project.cheerha.domain.jobopening.service.JobOpeningFindByService;
import com.project.cheerha.domain.user.entity.User;
import com.project.cheerha.domain.user.service.UserFindByService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserFindByService userFindByIdService;
    private final JobOpeningFindByService jobOpeningFindByService;
    private final BookmarkCacheService bookmarkCacheService;

    private static final int MAX_BOOKMARK_COUNT = 200;
    private static final int MAX_PAGES = 20;
    private boolean firstFetchFromDB = true; // 첫 번째 데이터 조회 여부를 추적하는 플래그

    /**
     * 사용자가 채용 공고를 북마크하는 메서드입니다.
     * 북마크가 이미 존재하면 아무 작업도 하지 않고, 최대 북마크 개수를 초과하면 가장 오래된 북마크를 삭제합니다.
     * 새로운 북마크를 DB에 저장하고, 캐시에도 반영합니다.
     *
     * @param userId 사용자 ID
     * @param jobOpeningId 채용 공고 ID
     */
    @Transactional
    public void createBookmark(Long userId, Long jobOpeningId) {
        if (bookmarkRepository.existsByUserIdAndJobOpeningId(userId, jobOpeningId)) {
            return;
        }
        long bookmarkCount = bookmarkRepository.countByUserId(userId);
        if (bookmarkCount >= MAX_BOOKMARK_COUNT) {
            Bookmark oldestBookmark = bookmarkRepository.findFirstByUserIdOrderByIdAsc(userId);
            bookmarkRepository.delete(oldestBookmark);
        }
        User user = userFindByIdService.findById(userId);
        JobOpening jobOpening = jobOpeningFindByService.findById(jobOpeningId);
        Bookmark bookmark = Bookmark.toEntity(user, jobOpening);
        bookmarkRepository.save(bookmark);
        // RedisBookmarkService로 캐시 업데이트
        ReadBookmarkResponseDto dto = ReadBookmarkResponseDto.toDto(bookmark);
        bookmarkCacheService.updateCacheOnBookmarkAdd(userId, dto);
    }

    /**
     * 사용자의 모든 북마크를 페이징 처리하여 조회하는 메서드입니다.
     * 캐시에 저장된 북마크를 우선 조회하고, 캐시에 없으면 DB에서 조회한 후 캐시에 저장합니다.
     *
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 북마크 리스트
     */
    @Transactional(readOnly = true)
    public Page<ReadBookmarkResponseDto> readAllBookmarks(Long userId, Pageable pageable) {
        // 페이지 번호가 20을 초과하면 20으로 설정
        int pageNumber = pageable.getPageNumber();
        if (pageNumber >= MAX_PAGES) {
            pageNumber = MAX_PAGES - 1; // 20페이지를 넘지 않도록 제한
        }
        Pageable limitedPageable = PageRequest.of(pageNumber, pageable.getPageSize());
        Page<Bookmark> bookmarkPage;
        if (firstFetchFromDB && pageNumber == 0) {
            // 첫 번째 페이지 데이터만 DB에서 조회하고 캐시에 저장
            bookmarkPage = fetchAndCacheBookmarks(userId, limitedPageable);
            firstFetchFromDB = false; // 첫 번째 페이지 조회 후 DB에서만 데이터를 가져오는 플래그 설정
        } else {
            // 2페이지부터는 캐시에서 조회하고, 캐시에 없으면 DB에서 가져옵니다.
            List<ReadBookmarkResponseDto> cachedDtos = bookmarkCacheService.getAllBookmarksFromCache(userId);
            if (!cachedDtos.isEmpty() && pageNumber == 0) {
                int skipCount = limitedPageable.getPageNumber() * limitedPageable.getPageSize();
                List<ReadBookmarkResponseDto> pagedDtos = cachedDtos.stream()
                        .skip(skipCount)
                        .limit(limitedPageable.getPageSize())
                        .toList();
                return new PageImpl<>(pagedDtos, pageable, cachedDtos.size());
            } else {
                bookmarkPage = fetchAndCacheBookmarks(userId, limitedPageable);
            }
        }
        // 페이지에 맞는 DTO 리스트로 변환
        List<ReadBookmarkResponseDto> dtoList = bookmarkPage.getContent().stream()
                .map(ReadBookmarkResponseDto::toDto)
                .collect(Collectors.toList());
        // 페이징된 결과를 PageImpl로 반환
        return new PageImpl<>(dtoList, pageable, bookmarkPage.getTotalElements());
    }

    /**
     * 사용자가 저장한 채용 공고의 북마크를 삭제하는 메서드입니다.
     */
    @Transactional  //TODO: 트랜잭션이 없으면 안되는 이유 찾기
    public void deleteBookmark(Long userId, Long jobOpeningId) {
        bookmarkRepository.deleteByUserIdAndJobOpeningId(userId, jobOpeningId);
        // 캐시에서 해당 북마크 삭제
        bookmarkCacheService.deleteBookmarkFromCache(userId, jobOpeningId);
    }

    /**
     * 커스텀 연령대 즐겨찾기 로직 중, 나이에 대한 예외처리가 진행됩니다.
     */
    public List<BookmarkCustomAgeResponseDto> readTop10BookmarkByAgeBetween(int minAge, int maxAge) {
        if (minAge > maxAge) {
            throw new BadRequestException(ClientErrorCode.MIN_AGE_EXCEEDS_MAX_AGE);
        }
        return bookmarkRepository.readTop10BookmarksByAgeBetween(minAge, maxAge);
    }

    /**
     * DB에서 데이터를 페이징 처리하여 가져오고 캐시에 저장하는 메서드입니다.
     */
    private Page<Bookmark> fetchAndCacheBookmarks(Long userId, Pageable pageable) {
        // DB에서 북마크들을 페이징 처리하여 가져옵니다.
        Page<Bookmark> bookmarkPage = bookmarkRepository.findByUserId(userId, pageable);
        // DB에서 가져온 데이터를 캐시에 저장
        List<Bookmark> bookmarks = bookmarkPage.getContent();
        for (Bookmark bookmark : bookmarks) {
            ReadBookmarkResponseDto dto = ReadBookmarkResponseDto.toDto(bookmark);
            bookmarkCacheService.updateCacheOnBookmarkAdd(userId, dto);
        }
        return bookmarkPage;
    }
}
