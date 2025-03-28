package com.project.cheerha.domain.datasync.service;

import com.project.cheerha.domain.document.entity.JobOpeningDocument;
import com.project.cheerha.domain.document.repository.JobOpeningDocumentRepository;
import com.project.cheerha.domain.jobopening.entity.JobOpening;
import com.project.cheerha.domain.jobopening.entity.RequiredSkills;
import com.project.cheerha.domain.jobopening.repository.JobOpeningRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MysqlToElasticsearchDataSync implements DataSync {

    private final JobOpeningRepository jobOpeningRepository;
    private final JobOpeningDocumentRepository jobOpeningDocumentRepository;

    /**
     * MySQL에서 채용 공고 데이터를 조회하여 Elasticsearch에 동기화하는 메서드입니다.
     *
     * 이 메서드는 MySQL에서 채용 공고와 관련된 키워드를 포함한 데이터를 가져오고,
     * 이를 Elasticsearch 형식에 맞게 변환하여 저장합니다.
     *
     * 1. MySQL에서 모든 채용 공고 데이터를 조회합니다.
     * 2. 각 채용 공고에 대해 관련된 키워드를 추출하여 `requiredSkills` 리스트에 추가합니다.
     * 3. `JobOpeningDocument` 객체로 변환하고 Elasticsearch에 저장합니다.
     */
    @Override
    @Transactional(readOnly = true)
    public void sync() {
        try {
            List<JobOpening> jobOpeningList = jobOpeningRepository.findAllWithJobOpeningKeywords();  // 적절한 ID 또는 쿼리 매개변수를 사용하여 조회
            log.info("총 {}개의 JobOpening 데이터를 조회했습니다.", jobOpeningList.size());

            List<JobOpeningDocument> jobOpeningDocuments = new ArrayList<>();

            // 각 JobOpening에 대해 키워드 리스트를 추출하여 requiredSkills에 추가
            for (JobOpening jobOpening : jobOpeningList) {
                RequiredSkills requiredSkills = new RequiredSkills(jobOpening.getJobOpeningKeywordList());

                // JobOpening을 JobOpeningDocument로 변환
                JobOpeningDocument jobOpeningDocument = JobOpeningDocument.create(jobOpening, requiredSkills);
                jobOpeningDocument.getRequiredSkills().addAll(requiredSkills.getValues());
                jobOpeningDocuments.add(jobOpeningDocument);
            }

            // Elasticsearch에 데이터 저장
            jobOpeningDocumentRepository.saveAll(jobOpeningDocuments);
            log.info("Mysql 데이터가 Elasticsearch에 성공적으로 동기화되었습니다");

        } catch (Exception e) {
            log.error("데이터 동기화 중 오류 발생", e);
        }
    }
}
