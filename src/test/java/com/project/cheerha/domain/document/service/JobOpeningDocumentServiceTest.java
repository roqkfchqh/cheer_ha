package com.project.cheerha.domain.document.service;

import com.project.cheerha.domain.document.dto.request.ReadJobOpeningDocumentRequestDto;
import com.project.cheerha.domain.document.dto.response.ReadJobOpeningDocumentResponseDto;
import com.project.cheerha.domain.document.entity.JobOpeningDocument;
import com.project.cheerha.domain.document.filter.JobOpeningDocumentFilter;
import com.project.cheerha.domain.document.repository.SearchDocumentRepository;
import com.project.cheerha.domain.searchhistory.service.SearchHistoryService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentMatchers;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JobOpeningDocumentServiceTest {

    @Mock
    private SearchDocumentRepository searchDocumentRepository;

    @Mock
    private SearchHistoryService searchHistoryService;

    @InjectMocks
    private JobOpeningDocumentService jobOpeningDocumentService;

    @Test
    void 전체_채용공고_조회_성공() {
        // Given: 테스트 데이터 준비
        // pageable 객체를 생성하고, 페이지 크기와 페이지 번호를 설정.
        Pageable pageable = mock(Pageable.class);
        int pageSize = 10;
        int pageNumber = 1;
        when(pageable.getPageSize()).thenReturn(pageSize);
        when(pageable.getPageNumber()).thenReturn(pageNumber);

        // Elasticsearch 요청 객체와 더미 데이터를 준비.
        JobOpeningDocument mockJobOpeningDocument = mock(JobOpeningDocument.class);
        List<JobOpeningDocument> jobOpeningDocuments = Collections.singletonList(mockJobOpeningDocument);
        when(searchDocumentRepository.fetchJobOpeningDocumentList(ArgumentMatchers.any())).thenReturn(jobOpeningDocuments);

        // When: 서비스 메소드 실행
        // 실제 메소드를 호출.
        Page<ReadJobOpeningDocumentResponseDto> result = jobOpeningDocumentService.readAllJobOpeningsUsingElasticsearch(pageable);

        // Then: 결과 검증
        // 반환된 페이지가 비어있지 않고, 예상한 크기와 일치하는지 확인.
        assertNotNull(result);
        assertEquals(jobOpeningDocuments.size(), result.getContent().size());

        // Elasticsearch의 fetchJobOpeningDocumentList 메소드가 정확히 한 번 호출되었는지 검증.
        verify(searchDocumentRepository, times(1)).fetchJobOpeningDocumentList(ArgumentMatchers.any());
    }

    @Test
    void 인기_채용공고_조회_성공() {
        // Given: 테스트 데이터 준비
        Pageable pageable = mock(Pageable.class);
        int pageSize = 10;
        int pageNumber = 1;
        when(pageable.getPageSize()).thenReturn(pageSize);
        when(pageable.getPageNumber()).thenReturn(pageNumber);

        // 더미 데이터 준비
        JobOpeningDocument mockJobOpeningDocument = mock(JobOpeningDocument.class);
        List<JobOpeningDocument> jobOpeningDocuments = Collections.singletonList(mockJobOpeningDocument);
        when(searchDocumentRepository.fetchJobOpeningDocumentList(ArgumentMatchers.any())).thenReturn(jobOpeningDocuments);

        // When: 서비스 메소드 실행
        Page<ReadJobOpeningDocumentResponseDto> result = jobOpeningDocumentService.readTop100PopularJobOpeningsUsingElasticsearch(pageable);

        // Then: 결과 검증
        assertNotNull(result);
        assertEquals(jobOpeningDocuments.size(), result.getContent().size());
        verify(searchDocumentRepository, times(1)).fetchJobOpeningDocumentList(ArgumentMatchers.any());
    }

    @Test
    void 필터링된_채용공고_조회_성공_검색어_포함() {
        // Given: 테스트 데이터 준비
        Pageable pageable = mock(Pageable.class);
        Long userId = 1L;
        String searchTerm = "developer";
        ReadJobOpeningDocumentRequestDto requestDto = mock(ReadJobOpeningDocumentRequestDto.class);
        when(requestDto.getSearchTerm()).thenReturn(searchTerm);
        when(pageable.getPageSize()).thenReturn(10);
        when(pageable.getPageNumber()).thenReturn(1);

        // 필터 객체 준비
        JobOpeningDocumentFilter filter = new JobOpeningDocumentFilter(requestDto);

        // 더미 데이터 준비
        JobOpeningDocument mockJobOpeningDocument = mock(JobOpeningDocument.class);
        List<JobOpeningDocument> jobOpeningDocuments = Collections.singletonList(mockJobOpeningDocument);
        when(searchDocumentRepository.fetchJobOpeningDocumentList(ArgumentMatchers.any())).thenReturn(jobOpeningDocuments);

        // When: 서비스 메소드 실행
        Page<ReadJobOpeningDocumentResponseDto> result = jobOpeningDocumentService.readJobOpeningUsingElasticSearchFilter(requestDto, userId, pageable);

        // Then: 결과 검증
        assertNotNull(result);
        assertEquals(jobOpeningDocuments.size(), result.getContent().size());
        verify(searchDocumentRepository, times(1)).fetchJobOpeningDocumentList(ArgumentMatchers.any());
        verify(searchHistoryService, times(1)).saveSearchTerm(userId, searchTerm); // 검색어 저장이 호출되었는지 검증
    }

    @Test
    void 필터링된_채용공고_조회_성공_검색어_없음() {
        // Given: 테스트 데이터 준비
        Pageable pageable = mock(Pageable.class);
        Long userId = 1L;
        ReadJobOpeningDocumentRequestDto requestDto = mock(ReadJobOpeningDocumentRequestDto.class);
        when(requestDto.getSearchTerm()).thenReturn(null);  // 검색어가 없는 경우
        when(pageable.getPageSize()).thenReturn(10);
        when(pageable.getPageNumber()).thenReturn(1);

        // 더미 데이터 준비
        JobOpeningDocument mockJobOpeningDocument = mock(JobOpeningDocument.class);
        List<JobOpeningDocument> jobOpeningDocuments = Collections.singletonList(mockJobOpeningDocument);
        when(searchDocumentRepository.fetchJobOpeningDocumentList(ArgumentMatchers.any())).thenReturn(jobOpeningDocuments);

        // When: 서비스 메소드 실행
        Page<ReadJobOpeningDocumentResponseDto> result = jobOpeningDocumentService.readJobOpeningUsingElasticSearchFilter(requestDto, userId, pageable);

        // Then: 결과 검증
        assertNotNull(result);
        assertEquals(jobOpeningDocuments.size(), result.getContent().size());
        verify(searchDocumentRepository, times(1)).fetchJobOpeningDocumentList(ArgumentMatchers.any());
        verify(searchHistoryService, times(0)).saveSearchTerm(userId, null);  // 검색어가 없으면 검색어 저장하지 않음
    }
}
