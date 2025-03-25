package com.project.cheerha.domain.bookmark.dto.response;

import com.project.cheerha.domain.bookmark.entity.Bookmark;
import com.project.cheerha.domain.jobopening.entity.JobOpening;
import com.project.cheerha.domain.jobopening.entity.RequiredSkills;

import java.util.List;

/**
 * 북마크 정보를 담기 위한 DTO 클래스입니다.
 * 사용자가 저장한 채용 공고에 대한 정보를 전달하는데 사용됩니다.
 */
public record ReadBookmarkResponseDto(
        Long id,
        String company,       // 회사명
        String hiringStartAt,  // 채용시작일
        String hiringEndAt,   // 채용마감일
        String position,      // 포지션 (직무명)
        List<String> requiredSkillList // 자격 요건 (기술 키워드 리스트)
) {

    /**
     * Bookmark 엔티티 객체를 DTO로 변환하는 메서드입니다.
     *
     * Bookmark 엔티티를 받아서, 그 안에 포함된 JobOpening 객체의 정보를
     * ReadBookmarkResponseDto 형식으로 정제하여 반환합니다.
     *
     * @param bookmark 변환할 Bookmark 객체
     * @return ReadBookmarkResponseDto 채용 공고 정보를 포함하는 DTO 객체
     */
    public static ReadBookmarkResponseDto toDto(Bookmark bookmark) {
        JobOpening jobOpening = bookmark.getJobOpening();  // Bookmark에서 JobOpening 정보를 가져옵니다.

        // JobOpening 객체의 정보를 DTO로 변환하여 반환합니다.
        return new ReadBookmarkResponseDto(
                jobOpening.getId(),
                jobOpening.getCompany(),           // 회사명
                jobOpening.getHiringStartAt().toString(),      // 채용시작일
                jobOpening.getHiringEndAt().toString(),      // 채용마감일
                jobOpening.getPosition(),          // 포지션 (직무명)
                new RequiredSkills(jobOpening.getJobOpeningKeywordList()).getValues()   // 자격 요건 (기술 키워드 리스트)
        );
    }
}
