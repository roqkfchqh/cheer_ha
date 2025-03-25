package com.project.cheerha.domain.document.dto.response;

import com.project.cheerha.domain.document.entity.JobOpeningDocument;

import java.util.List;
import java.util.stream.Collectors;

public record ReadJobOpeningDocumentAutoResponseDto(
    String id,
    String title,
    String company,
    List<String> requiredSkillList
) {
    /**
     * 주어진 `JobOpeningDocument` 목록을 `ReadJobOpeningDocumentAutoResponseDto` 목록으로 변환하는 팩토리 메서드입니다.
     *
     * 이 메서드는 `JobOpeningDocument` 엔티티의 각 필드를 `ReadJobOpeningDocumentAutoResponseDto`로 매핑하여,
     * 클라이언트에 전달할 수 있는 DTO 객체 리스트를 생성합니다.
     *
     * @param jobOpeningDocumentList 변환할 `JobOpeningDocument` 객체의 리스트
     * @return 변환된 `ReadJobOpeningDocumentAutoResponseDto` 객체의 리스트
     */
    public static List<ReadJobOpeningDocumentAutoResponseDto> toDto(List<JobOpeningDocument> jobOpeningDocumentList) {
        return jobOpeningDocumentList.stream()
            .map(job -> new ReadJobOpeningDocumentAutoResponseDto(
                job.getId(),
                job.getTitle(),
                job.getCompany(),
                job.getRequiredSkills().stream().distinct().collect(Collectors.toList())
            ))
            .collect(Collectors.toList());
    }
}
