package com.project.cheerha.domain.jobopening.entity;

import com.project.cheerha.domain.keyword.entity.JobOpeningKeyword;
import com.project.cheerha.domain.keyword.entity.Keyword;
import lombok.Value;

import java.util.List;

@Value
public class RequiredSkills {
    List<String> values;

    public RequiredSkills(List<JobOpeningKeyword> jobOpeningKeywords) {
        this.values = jobOpeningKeywords.stream()
                .map(JobOpeningKeyword::getKeyword)
                .filter(java.util.Objects::nonNull)
                .map(Keyword::getName)
                .toList();
    }

    public RequiredSkills(List<String> skillNames, boolean alreadyProcessed) {
        this.values = List.copyOf(skillNames);
    }
}