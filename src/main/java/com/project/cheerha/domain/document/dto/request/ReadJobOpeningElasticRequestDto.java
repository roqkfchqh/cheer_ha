package com.project.cheerha.domain.document.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ReadJobOpeningElasticRequestDto {

    private String educationLevel;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime hiringStartAt;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime hiringEndAt;

    private String location;
    private Integer minExperienceYears;
    private Integer maxExperienceYears;
    private String employmentType;
    private String requiredSkill;

    @Pattern(regexp = "^(|.*\\S.*)$", message = "검색어는 공백만 포함할 수 없습니다.")
    private String searchTerm;
}
