package com.project.cheerha.domain.document.dto.response;

import com.project.cheerha.domain.document.entity.JobOpeningDocument;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ReadJobOpeningElasticResponseDto 레코드는 Elasticsearch에서 조회한
 * 채용공고(Job Opening) 정보를 담는 DTO(Data Transfer Object)입니다.
 * 이 DTO는 클라이언트에 채용공고의 세부 정보를 전달하는 데 사용됩니다.
 */
public record ReadJobOpeningElasticResponseDto(
        String id,  // 엘라스틱서치 id
        String title, // 채용공고 제목
        String company, // 회사 이름
        String location, // 채용 지역
        Integer salary, // 연봉
        String employmentType, // 고용 형태 (예: 정규직, 계약직 등)
        String educationLevel, // 요구하는 학력 수준
        String jobOpeningUrl, // 채용공고 URL
        Integer minExperienceYears, // 최소 경력 연수
        Integer maxExperienceYears, // 최대 경력 연수
        String position, // 채용 직무
        ZonedDateTime hiringStartAt, // 채용 시작일
        ZonedDateTime hiringEndAt, // 채용 마감일
        ZonedDateTime createdAt, // 채용공고 생성일
        Integer viewCount, // 조회수
        List<String> requiredSkillList // 요구되는 기술 목록
) {
    /**
     * 주어진 `JobOpeningDocument` 목록을 `ReadJobOpeningElasticResponseDto` 목록으로 변환하는 팩토리 메서드입니다.
     *
     * 이 메서드는 `JobOpeningDocument` 엔티티의 각 필드를 `ReadJobOpeningElasticResponseDto`로 매핑하여,
     * 클라이언트에 전달할 수 있는 DTO 객체 리스트를 생성합니다.
     *
     * @param jobOpeningDocumentList 변환할 `JobOpeningDocument` 객체의 리스트
     * @return 변환된 `ReadJobOpeningElasticResponseDto` 객체의 리스트
     */
    public static List<ReadJobOpeningElasticResponseDto> toDto(List<JobOpeningDocument> jobOpeningDocumentList) {
        return jobOpeningDocumentList.stream()
                .map(job -> new ReadJobOpeningElasticResponseDto(
                        job.getId(),
                        job.getTitle(),
                        job.getCompany(),
                        job.getLocation(),
                        job.getSalary(),
                        job.getEmploymentType(),
                        job.getEducationLevel(),
                        job.getJobOpeningUrl(),
                        job.getMinExperienceYears(),
                        job.getMaxExperienceYears(),
                        job.getPosition(),
                        job.getHiringStartAt(),
                        job.getHiringEndAt(),
                        job.getCreatedAt(),
                        job.getViewCount(),
                        job.getRequiredSkills().stream().distinct().collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }
}
