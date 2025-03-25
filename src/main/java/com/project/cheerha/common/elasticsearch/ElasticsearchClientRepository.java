package com.project.cheerha.common.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.project.cheerha.domain.document.entity.JobOpeningDocument;
import com.project.cheerha.common.exception.data.ElasticsearchQueryException;
import com.project.cheerha.common.exception.data.DataErrorCode;
import com.project.cheerha.domain.document.repository.SearchDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ElasticsearchClientRepository implements SearchDocumentRepository {

    private final ElasticsearchClient elasticsearchClient;

    /**
     * Elasticsearch에서 데이터를 검색하는 메서드
     *
     * @param searchRequest Elasticsearch 쿼리 요청 객체
     * @return 검색된 데이터 목록
     * @throws ElasticsearchQueryException Elasticsearch 쿼리 실행 실패 시 발생
     */
    @Override
    public List<JobOpeningDocument> fetchJobOpeningDocumentList(SearchRequest searchRequest) {
        try {
            // Elasticsearch 쿼리 실행
            SearchResponse<JobOpeningDocument> searchResponse = elasticsearchClient.search(searchRequest, JobOpeningDocument.class);
            return searchResponse.hits().hits().stream()
                    .map(hit -> hit.source())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new ElasticsearchQueryException(DataErrorCode.JOB_OPENING_NOT_FOUND);
        }
    }

    /**
     * Elasticsearch에서 검색된 총 문서 수를 반환하는 메서드
     *
     * @param searchRequest Elasticsearch 쿼리 요청 객체
     * @return 검색된 총 문서 수
     * @throws ElasticsearchQueryException Elasticsearch 쿼리 실행 실패 시 발생
     */
    @Override
    public long getTotalCount(SearchRequest searchRequest) {
        try {
            SearchResponse<JobOpeningDocument> searchResponse = elasticsearchClient.search(searchRequest, JobOpeningDocument.class);
            // 검색된 총 문서 수 반환
            return searchResponse.hits().total().value();
        } catch (IOException e) {
            throw new ElasticsearchQueryException(DataErrorCode.JOB_OPENING_NOT_FOUND);
        }
    }
}
