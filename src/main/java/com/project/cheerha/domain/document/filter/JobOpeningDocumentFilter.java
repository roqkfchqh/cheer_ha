package com.project.cheerha.domain.document.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import com.project.cheerha.common.elasticsearch.IndexName;
import com.project.cheerha.domain.document.dto.request.ReadJobOpeningElasticRequestDto;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JobOpeningDocumentFilter {

    private final ReadJobOpeningElasticRequestDto requestDto;

    public BoolQuery.Builder build() {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // 지역 필터링
        if (requestDto.getLocation() != null) {
            boolQueryBuilder.filter(f -> f
                    .term(t -> t
                            .field(IndexName.LOCATION)
                            .value(requestDto.getLocation())
                    )
            );
        }

        // 고용 형태 필터링
        if (requestDto.getEmploymentType() != null) {
            boolQueryBuilder.filter(f -> f
                    .term(t -> t
                            .field(IndexName.EMPLOYMENT_TYPE)
                            .value(requestDto.getEmploymentType())
                    )
            );
        }

        // 학력 필터링
        if (requestDto.getEducationLevel() != null) {
            boolQueryBuilder.filter(f -> f
                    .term(t -> t
                            .field(IndexName.EDUCATION_LEVEL)
                            .value(requestDto.getEducationLevel())
                    )
            );
        }

        // 최소 경력 필터링
        if (requestDto.getMinExperienceYears() != null) {
            boolQueryBuilder.filter(f -> f
                    .range(RangeQuery.of(r -> r
                            .term(t -> t
                                    .field(IndexName.MIN_EXPERIENCE_YEARS)
                                    .gte(String.valueOf(requestDto.getMinExperienceYears()))
                            )
                    ))
            );
        }

        // 최대 경력 필터링
        if (requestDto.getMaxExperienceYears() != null) {
            boolQueryBuilder.filter(f -> f
                    .range(RangeQuery.of(r -> r
                            .term(t -> t
                                    .field(IndexName.MAX_EXPERIENCE_YEARS)
                                    .lte(String.valueOf(requestDto.getMaxExperienceYears()))
                            )
                    ))
            );
        }

        // 채용 시작 날짜 필터링
        if (requestDto.getHiringStartAt() != null) {
            boolQueryBuilder.filter(f -> f
                    .range(RangeQuery.of(r -> r
                            .term(t -> t
                                    .field(IndexName.HIRING_START_AT)
                                    .gte(String.valueOf(requestDto.getHiringStartAt()))
                            )
                    ))
            );
        }

        // 채용 마감 날짜 필터링
        if (requestDto.getHiringEndAt() != null) {
            boolQueryBuilder.filter(f -> f
                    .range(RangeQuery.of(r -> r
                            .term(t -> t
                                    .field(IndexName.HIRING_END_AT)
                                    .lte(String.valueOf(requestDto.getHiringEndAt()))
                            )
                    ))
            );
        }

        // 자격 요건 필터링
        if (requestDto.getRequiredSkill() != null) {
            boolQueryBuilder.should(s -> s
                    .fuzzy(f -> f
                            .field(IndexName.REQUIRED_SKILLS) // fuzzy를 적용할 필드
                            .value(requestDto.getRequiredSkill())
                            .fuzziness("AUTO")
                    )
            );
        }

        // 검색어 조회
        if (requestDto.getSearchTerm() != null) {
            // 1순위: title 필드
            boolQueryBuilder.should(s -> s
                    .fuzzy(f -> f
                            .field(IndexName.TITLE) // fuzzy를 적용할 필드
                            .value(requestDto.getSearchTerm())
                            .fuzziness("AUTO")
                            .boost(2.0f) // title 우선순위 2배 증가
                    )
            );
            boolQueryBuilder.should(s -> s
                    .match(m -> m
                            .field(IndexName.TITLE)
                            .query(requestDto.getSearchTerm())
                            .operator(Operator.And)
                            .boost(2.0f) // title 우선순위 2배 증가
                    )
            );
            // 한글 형태소 검색
            boolQueryBuilder.should(s -> s
                .match(m -> m
                    .field(IndexName.TITLE + IndexName.MORPH_SUFFIX)
                    .query(requestDto.getSearchTerm())
                    .boost(2.0f) // title 우선순위 2배 증가
                )
            );

            // 2순위: company 필드
            boolQueryBuilder.should(s -> s
                    .fuzzy(f -> f
                            .field(IndexName.COMPANY) // fuzzy를 적용할 필드
                            .value(requestDto.getSearchTerm())
                            .fuzziness("AUTO")
                            .boost(1.5f) // company 우선순위 1.5배 증가
                    )
            );
            boolQueryBuilder.should(s -> s
                    .match(m -> m
                            .field(IndexName.COMPANY)
                            .query(requestDto.getSearchTerm())
                            .operator(Operator.And)
                            .boost(1.5f) // company 우선순위 1.5배 증가
                    )
            );
            // 한글 형태소 검색
            boolQueryBuilder.should(s -> s
                .match(m -> m
                    .field(IndexName.COMPANY + IndexName.MORPH_SUFFIX)
                    .query(requestDto.getSearchTerm())
                    .boost(1.5f) // company 우선순위 1.5배 증가
                )
            );

            // 3순위: REQUIRED_SKILLS 필드 (fuzzy 적용)
            boolQueryBuilder.should(s -> s
                    .fuzzy(f -> f
                            .field(IndexName.REQUIRED_SKILLS) // fuzzy를 적용할 필드
                            .value(requestDto.getSearchTerm())
                            .fuzziness("AUTO")
                            .boost(1.0f)
                    )
            );
            boolQueryBuilder.should(s -> s
                    .match(m -> m
                            .field(IndexName.REQUIRED_SKILLS)
                            .query(requestDto.getSearchTerm())
                            .operator(Operator.And)
                            .boost(1.0f)
                    )
            );
        }

        return boolQueryBuilder;
    }
}