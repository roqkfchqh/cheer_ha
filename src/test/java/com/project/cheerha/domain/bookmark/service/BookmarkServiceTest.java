package com.project.cheerha.domain.bookmark.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.project.cheerha.domain.bookmark.dto.response.ReadBookmarkResponseDto;
import com.project.cheerha.domain.bookmark.entity.Bookmark;
import com.project.cheerha.domain.bookmark.repository.BookmarkRepository;
import com.project.cheerha.domain.jobopening.entity.EducationLevel;
import com.project.cheerha.domain.jobopening.entity.EmploymentType;
import com.project.cheerha.domain.jobopening.entity.JobOpening;
import com.project.cheerha.domain.jobopening.service.JobOpeningFindByService;
import com.project.cheerha.domain.user.entity.User;
import com.project.cheerha.domain.user.service.UserFindByService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private UserFindByService userFindByService;

    @Mock
    private JobOpeningFindByService jobOpeningFindByService;

    @Mock
    private BookmarkCacheService bookmarkCacheService;

    @InjectMocks
    private BookmarkService bookmarkService;

    private User user;
    private JobOpening jobOpening;

    @BeforeEach
    void setUp() {
        Long userId = 1L;
        user = User.toEntity(
                "user2001@gmail.com",
                "user2001",
                30,
                6,
                "password123"
        );

        Long jobOpeningId = 1L;
        jobOpening = JobOpening.toEntity(
                "Software Engineer",
                "네이버",
                "서울",
                60000,
                EmploymentType.toEnum("정규직"),
                EducationLevel.toEnum("학사"),
                "http://example.com/job/1",
                0,
                3,
                "Software Engineer",
                ZonedDateTime.now(),
                ZonedDateTime.now().plusDays(30)
        );
    }

    @Test
    void 북마크_생성_성공() {
        Bookmark bookmark = Bookmark.toEntity(user, jobOpening);

        when(bookmarkRepository.existsByUserIdAndJobOpeningId(user.getId(), jobOpening.getId())).thenReturn(false);
        when(bookmarkRepository.countByUserId(user.getId())).thenReturn(0L);
        when(jobOpeningFindByService.findById(jobOpening.getId())).thenReturn(jobOpening);

        // When: 북마크를 저장할 때
        bookmarkService.createBookmark(user.getId(), jobOpening.getId());

        // Then: save 메서드가 1번 호출되었는지, 캐시 업데이트가 1번 호출되었는지 확인
        verify(bookmarkRepository, times(1)).save(any(Bookmark.class));
        verify(bookmarkCacheService, times(1)).updateCacheOnBookmarkAdd(user.getId(), ReadBookmarkResponseDto.toDto(bookmark));
    }

    @Test
    void 북마크_조회_성공() {
        // Given: 북마크 생성
        Bookmark bookmark = Bookmark.toEntity(user, jobOpening);
        List<Bookmark> bookmarks = List.of(bookmark);
        Page<Bookmark> bookmarkPage = new PageImpl<>(bookmarks);

        // When: 첫 번째 페이지 조회 시 캐시가 비어있고 DB에서 조회되는 경우
        when(bookmarkRepository.findByUserId(user.getId(), PageRequest.of(0, 10))).thenReturn(bookmarkPage);
        when(bookmarkCacheService.getAllBookmarksFromCache(user.getId())).thenReturn(new ArrayList<>()); // 캐시 비어 있음

        // 첫 번째 페이지 조회 시 DB에서 데이터를 가져오고 캐시 업데이트
        Page<ReadBookmarkResponseDto> result = bookmarkService.readAllBookmarks(user.getId(), PageRequest.of(0, 10));

        // 첫 번째 페이지 재 조회 시 캐시에서 데이터를 가져옴
        List<ReadBookmarkResponseDto> cachedBookmarks = bookmarkCacheService.getAllBookmarksFromCache(user.getId());
        Page<ReadBookmarkResponseDto> cachedResult = new PageImpl<>(cachedBookmarks);

        // Then: 캐시에서 조회된 북마크 목록의 size가 1이어야 하며, 회사명이 '네이버'이어야 함
        assertEquals(1, result.getTotalElements());
        assertEquals("네이버", result.getContent().get(0).company());

        // 캐시 조회가 1번 호출되었는지 확인
        verify(bookmarkCacheService, times(1)).getAllBookmarksFromCache(user.getId());

        // 두 번째 페이지 조회 시, 캐시에서 조회가 아닌 DB에서 가져와야 함
        when(bookmarkCacheService.getAllBookmarksFromCache(user.getId())).thenReturn(cachedBookmarks); // 첫 페이지 캐시에서 가져오기
        // 두 번째 페이지를 올바르게 모킹하고, 총 요소 수를 정확하게 설정
        // DB에서 10개 항목과 캐시에서 1개 항목을 합쳐서 총 11개의 요소가 있어야 함
        when(bookmarkRepository.findByUserId(user.getId(), PageRequest.of(1, 10)))
                .thenReturn(new PageImpl<>(bookmarks, PageRequest.of(1, 10), 11));  // DB에서 10개 항목, 총 11개(캐시 포함)

        // 두 번째 페이지 조회 (DB에서 조회)
        Page<ReadBookmarkResponseDto> resultFromDB = bookmarkService.readAllBookmarks(user.getId(), PageRequest.of(1, 10));

        // Then: 두 번째 페이지가 DB에서 조회되었는지 확인
        assertEquals(11, resultFromDB.getTotalElements());  // 총 요소는 11개여야 함 (DB에서 10개 + 캐시에서 1개)

        // DB에서 두 번째 페이지가 호출되었는지 확인
        verify(bookmarkRepository, times(1)).findByUserId(user.getId(), PageRequest.of(1, 10));  // 두 번째 페이지는 DB에서 호출되어야 함
    }

    @Test
    void 북마크_삭제_성공() {
        Bookmark bookmark = Bookmark.toEntity(user, jobOpening);

        // When: 북마크 삭제를 요청할 때
        bookmarkService.deleteBookmark(user.getId(), jobOpening.getId());

        // Then: deleteByUserIdAndJobOpeningId 메서드가 1번 호출되었고, 캐시 삭제도 이루어졌는지 확인
        verify(bookmarkRepository, times(1)).deleteByUserIdAndJobOpeningId(user.getId(), jobOpening.getId());
        verify(bookmarkCacheService, times(1)).deleteBookmarkFromCache(user.getId(), jobOpening.getId());
    }

    @Test
    void 북마크_최대_200개_초과시_가장_오래된_북마크_삭제() {
        when(bookmarkRepository.existsByUserIdAndJobOpeningId(user.getId(), jobOpening.getId())).thenReturn(false);
        when(bookmarkRepository.countByUserId(user.getId())).thenReturn(200L);

        // Mock 가장 오래된 북마크 삭제
        Bookmark oldestBookmark = mock(Bookmark.class);
        Bookmark latestBookmark = Bookmark.toEntity(user, jobOpening);
        when(bookmarkRepository.findFirstByUserIdOrderByIdAsc(user.getId())).thenReturn(oldestBookmark);
        when(jobOpeningFindByService.findById(jobOpening.getId())).thenReturn(jobOpening);

        // When: 새로운 북마크를 생성하려고 할 때
        bookmarkService.createBookmark(user.getId(), jobOpening.getId());

        // Then: 가장 오래된 북마크가 삭제되고 새 북마크가 저장되어야 함
        verify(bookmarkRepository, times(1)).delete(oldestBookmark);
        verify(bookmarkRepository, times(1)).save(any(Bookmark.class));
        verify(bookmarkCacheService, times(1)).updateCacheOnBookmarkAdd(user.getId(), ReadBookmarkResponseDto.toDto(latestBookmark));
    }
}
