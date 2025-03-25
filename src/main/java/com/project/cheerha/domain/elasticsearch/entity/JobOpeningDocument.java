package com.project.cheerha.domain.elasticsearch.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.cheerha.common.elasticsearch.IndexName;
import com.project.cheerha.domain.jobopening.entity.JobOpening;
import com.project.cheerha.domain.jobopening.entity.RequiredSkills;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Document(indexName = IndexName.JOB_OPENING_DOCUMENT)
public class JobOpeningDocument {

    @Id
    @Field(type = FieldType.Keyword)
    private String id; // Elasticsearch ID 필드

    @Field(type = FieldType.Text)  // Text 타입으로 저장
    private String title; // 직무 제목

    @Field(type = FieldType.Text)  // Text 타입으로 저장
    private String company; // 회사 이름

    @Field(type = FieldType.Keyword)  // Text 타입으로 저장
    private String location; // 근무지

    @Field(type = FieldType.Integer)  // Integer 타입으로 저장
    private int salary; // 연봉

    @Field(type = FieldType.Keyword)  // Text 타입으로 저장
    private String employmentType; // 고용 형태

    @Field(type = FieldType.Keyword)  // Text 타입으로 저장
    private String educationLevel; // 교육 수준

    @Field(type = FieldType.Text)  // Text 타입으로 저장
    private String jobOpeningUrl; // 채용 공고 URL

    @Field(type = FieldType.Integer)  // Integer 타입으로 저장
    private Integer minExperienceYears; // 최소 경력 연수

    @Field(type = FieldType.Integer)  // Integer 타입으로 저장
    private Integer maxExperienceYears; // 최대 경력 연수

    @Field(type = FieldType.Text)  // Text 타입으로 저장
    private String position; // 직무

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    @Field(type = FieldType.Date)  // Date 타입으로 저장
    private ZonedDateTime hiringStartAt; // 채용 시작일

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    @Field(type = FieldType.Date)  // Date 타입으로 저장
    private ZonedDateTime hiringEndAt; // 채용 종료일

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    @Field(type = FieldType.Date)  // Date 타입으로 저장
    private ZonedDateTime createdAt; // 채용공고 생성일

    @Field(type = FieldType.Integer)  // Integer 타입으로 저장
    private int viewCount; // 조회수

    @Field(type = FieldType.Text)  // fuzzy 지원을 위해 text로 변경
    private List<String> requiredSkills; // 자격 요건 키워드 리스트

    /**
     * JobOpening 엔티티를 기반으로 JobOpeningDocument 객체를 생성하는 메서드입니다.
     *
     * 이 메서드는 JobOpening 엔티티에서 필요한 정보를 추출하여 Elasticsearch에서 저장할 수 있는
     * `JobOpeningDocument` 객체로 변환합니다. 변환된 객체는 Elasticsearch에 저장됩니다.
     *
     * @param jobOpening 변환할 JobOpening 엔티티 객체
     * @return 변환된 JobOpeningDocument 객체
     */
    public static JobOpeningDocument create(JobOpening jobOpening, RequiredSkills requiredSkills) {
        return new JobOpeningDocument(
                jobOpening.getId().toString(), // jobOpening의 id를 String으로 변환하여 사용
                jobOpening.getTitle(),
                jobOpening.getCompany(),
                jobOpening.getLocation(),
                jobOpening.getSalary(),
                jobOpening.getEmploymentType().name(),
                jobOpening.getEducationLevel().name(),
                jobOpening.getJobOpeningUrl(),
                jobOpening.getMinExperienceYears(),
                jobOpening.getMaxExperienceYears(),
                jobOpening.getPosition(),
                jobOpening.getHiringStartAt(),
                jobOpening.getHiringEndAt(),
                jobOpening.getCreatedAt(),
                jobOpening.getViewCount(),
                requiredSkills.getValues()
        );
    }
}