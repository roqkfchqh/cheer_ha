package com.project.cheerha.domain.jobopening.dto.response;

import com.project.cheerha.domain.jobopening.entity.RequiredSkills;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
public class ReadJobOpeningResponseDto {

    private final Long id;
    private final String title;
    private final String company;
    private final String location;
    private final int salary;
    private final String employmentType;
    private final String educationLevel;
    private final String jobOpeningUrl;
    private final Integer minExperienceYears;
    private final Integer maxExperienceYears;
    private final String position;
    private final ZonedDateTime hiringStartAt;
    private final ZonedDateTime hiringEndAt;
    private final ZonedDateTime createdAt;
    private final int viewCount;

    private List<String> requiredSkillList;

    @QueryProjection
    public ReadJobOpeningResponseDto(
            Long id, String title,
            String company, String location,
            int salary, String employmentType,
            String educationLevel, String jobOpeningUrl,
            Integer minExperienceYears, Integer maxExperienceYears,
            String position, ZonedDateTime hiringStartAt, ZonedDateTime hiringEndAt,
            ZonedDateTime createdAt, int viewCount
    ) {
        this.id = id;
        this.title = title;
        this.company = company;
        this.location = location;
        this.salary = salary;
        this.employmentType = employmentType;
        this.educationLevel = educationLevel;
        this.jobOpeningUrl = jobOpeningUrl;
        this.minExperienceYears = minExperienceYears;
        this.maxExperienceYears = maxExperienceYears;
        this.position = position;
        this.hiringStartAt = hiringStartAt;
        this.hiringEndAt = hiringEndAt;
        this.createdAt = createdAt;
        this.viewCount = viewCount;
    }

    public void addRequiredSkills(RequiredSkills requiredSkills) {
        this.requiredSkillList = requiredSkills.getValues();
    }
}
