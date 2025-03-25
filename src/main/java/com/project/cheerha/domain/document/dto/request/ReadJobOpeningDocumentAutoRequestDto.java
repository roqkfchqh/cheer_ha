package com.project.cheerha.domain.document.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReadJobOpeningDocumentAutoRequestDto {

    @Pattern(regexp = "^(|.*\\S.*)$", message = "검색어는 공백만 포함할 수 없습니다.")
    private String searchTerm;
}
