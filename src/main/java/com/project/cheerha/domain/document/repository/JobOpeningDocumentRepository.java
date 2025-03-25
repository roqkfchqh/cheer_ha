package com.project.cheerha.domain.document.repository;

import com.project.cheerha.domain.document.entity.JobOpeningDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface JobOpeningDocumentRepository extends ElasticsearchRepository<JobOpeningDocument, String> {
}
