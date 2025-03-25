package com.project.cheerha.domain.document.repository;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.project.cheerha.domain.document.entity.JobOpeningDocument;

import java.util.List;

public interface SearchDocumentRepository {

    List<JobOpeningDocument> fetchJobOpeningDocumentList(SearchRequest searchRequest);

    long getTotalCount(SearchRequest searchRequest);
}
