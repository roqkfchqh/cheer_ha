package com.project.cheerha.domain.document.service;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.project.cheerha.common.elasticsearch.IndexName;
import com.project.cheerha.domain.document.dto.request.ReadJobOpeningDocumentAutoRequestDto;
import com.project.cheerha.domain.document.dto.request.ReadJobOpeningDocumentRequestDto;
import com.project.cheerha.domain.document.dto.response.ReadJobOpeningDocumentAutoResponseDto;
import com.project.cheerha.domain.document.dto.response.ReadJobOpeningDocumentResponseDto;
import com.project.cheerha.domain.document.entity.JobOpeningDocument;
import com.project.cheerha.domain.document.filter.JobOpeningDocumentAutoFilter;
import com.project.cheerha.domain.document.filter.JobOpeningDocumentFilter;
import com.project.cheerha.domain.document.repository.SearchDocumentRepository;
import com.project.cheerha.domain.searchhistory.service.SearchHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobOpeningDocumentService {

    private final SearchDocumentRepository searchDocumentRepository;
    private final SearchHistoryService searchHistoryService;

    /**
     * 전체 채용공고 조회
     */
    @Transactional(readOnly = true)
    public Page<ReadJobOpeningDocumentResponseDto> readAllJobOpeningsUsingElasticsearch(Pageable pageable) {
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        int from = calculateFrom(pageNumber, pageSize, IndexName.MAX_JOB_OPENING_SIZE);
        SearchRequest searchRequest = buildSearchRequest(from, pageSize, null);
        List<JobOpeningDocument> jobOpeningDocumentList = searchDocumentRepository.fetchJobOpeningDocumentList(searchRequest);
        long totalJobOpenings = searchDocumentRepository.getTotalCount(searchRequest);
        List<ReadJobOpeningDocumentResponseDto> dtoList = ReadJobOpeningDocumentResponseDto.toDto(jobOpeningDocumentList);
        return new PageImpl<>(dtoList, pageable, totalJobOpenings);
    }

    /**
     * 조회수 기준 인기 채용공고 조회
     */
    @Transactional(readOnly = true)
    public Page<ReadJobOpeningDocumentResponseDto> readTop100PopularJobOpeningsUsingElasticsearch(Pageable pageable) {
        int pageSize = Math.min(pageable.getPageSize(), IndexName.MAX_POPULAR_SIZE);
        int pageNumber = pageable.getPageNumber();
        int from = calculateFrom(pageNumber, pageSize, IndexName.MAX_POPULAR_SIZE);
        SearchRequest searchRequest = buildSearchRequest(from, pageSize, IndexName.VIEW_COUNT);
        List<JobOpeningDocument> jobOpeningDocuments = searchDocumentRepository.fetchJobOpeningDocumentList(searchRequest);
        List<ReadJobOpeningDocumentResponseDto> dtoList = ReadJobOpeningDocumentResponseDto.toDto(jobOpeningDocuments);
        return new PageImpl<>(dtoList, pageable, IndexName.MAX_POPULAR_SIZE);
    }

    /**
     * 필터링된 채용공고 조회
     */
    @Transactional
    public Page<ReadJobOpeningDocumentResponseDto> readJobOpeningUsingElasticSearchFilter(
            ReadJobOpeningDocumentRequestDto requestDto,
            Long userId,
            Pageable pageable
    ) {
        if (requestDto.getSearchTerm() != null) {
            searchHistoryService.saveSearchTerm(userId, requestDto.getSearchTerm());
        }
        JobOpeningDocumentFilter filter = new JobOpeningDocumentFilter(requestDto);
        var boolQueryBuilder = filter.build();
        SearchRequest searchRequest = getSearchRequest(pageable, boolQueryBuilder);
        List<JobOpeningDocument> jobOpeningDocumentList = searchDocumentRepository.fetchJobOpeningDocumentList(searchRequest);
        long totalCount = searchDocumentRepository.getTotalCount(searchRequest);
        List<ReadJobOpeningDocumentResponseDto> dtoList = ReadJobOpeningDocumentResponseDto.toDto(jobOpeningDocumentList);
        return new PageImpl<>(dtoList, pageable, totalCount);
    }

    /**
     * 자동 완성 기능을 통한 채용 공고 조회
     */
    @Transactional
    public Page<ReadJobOpeningDocumentAutoResponseDto> readJobOpeningElasticAuto(
            ReadJobOpeningDocumentAutoRequestDto requestDto,
            Long userId,
            Pageable pageable
    ) {
        if (requestDto.getSearchTerm() != null) {
            searchHistoryService.saveSearchTerm(userId, requestDto.getSearchTerm());
        }

        JobOpeningDocumentAutoFilter filter = new JobOpeningDocumentAutoFilter(requestDto);
        var boolQueryBuilder = filter.build();
        SearchRequest searchRequest = getSearchRequest(pageable, boolQueryBuilder);
        List<JobOpeningDocument> jobOpeningDocumentList = searchDocumentRepository.fetchJobOpeningDocumentList(searchRequest);
        long totalCount = searchDocumentRepository.getTotalCount(searchRequest);
        List<ReadJobOpeningDocumentAutoResponseDto> dtoList = ReadJobOpeningDocumentAutoResponseDto.toDto(jobOpeningDocumentList);
        return new PageImpl<>(dtoList, pageable, totalCount);
    }

    /**
     * 페이지 번호와 페이지 크기를 바탕으로 Elasticsearch의 "from" 값을 계산하는 메서드
     */
    private int calculateFrom(int pageNumber, int pageSize, int maxSize) {
        int totalPages = (int) Math.ceil((double) maxSize / pageSize);
        if (pageNumber >= totalPages) {
            pageNumber = totalPages - 1;
        }
        return pageNumber * pageSize;
    }

    /**
     * Elasticsearch 쿼리 요청을 설정하는 메서드
     */
    private SearchRequest buildSearchRequest(int from, int pageSize, String sortField) {
        SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder()
                .index(IndexName.JOB_OPENING_DOCUMENT)
                .from(from)
                .size(pageSize);
        if (sortField != null) {
            searchRequestBuilder.sort(s -> s.field(f -> f.field(sortField).order(SortOrder.Desc)));
        }
        // 마감된 채용공고는 조회되지 않도록 처리
        filterExpiredJobOpenings(searchRequestBuilder);
        return searchRequestBuilder.build();
    }

    /**
     * 마감된 채용공고를 조회되지 않도록 필터링하는 메서드
     */
    private void filterExpiredJobOpenings(SearchRequest.Builder searchRequestBuilder) {
        searchRequestBuilder.query(q -> q
                .range(RangeQuery.of(r -> r
                                .term(t -> t
                                        .field(IndexName.HIRING_END_AT)
                                        .gte(IndexName.CURRENT_DATE_TIME)
                                )
                        )
                )
        );
    }

    private SearchRequest getSearchRequest(Pageable pageable, BoolQuery.Builder boolQueryBuilder) {
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        int from = calculateFrom(pageNumber, pageSize, IndexName.MAX_JOB_OPENING_SIZE);
        return new SearchRequest.Builder()
                .index(IndexName.JOB_OPENING_DOCUMENT)
                .query(q -> q.bool(boolQueryBuilder.build()))
                .sort(s -> s.field(f -> f.field(IndexName.CREATED_AT).order(SortOrder.Desc)))
                .from(from)
                .size(pageSize)
                .build();
    }
}
