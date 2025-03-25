package com.project.cheerha.domain.document.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import com.project.cheerha.common.elasticsearch.IndexName;
import com.project.cheerha.domain.document.dto.request.ReadJobOpeningElasticAutoRequestDto;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JobOpeningDocumentAutoFilter {

    private final ReadJobOpeningElasticAutoRequestDto requestDto;

    public BoolQuery.Builder build() {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

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
            // 자동 완성 검색
            boolQueryBuilder.should(s -> s
                .matchPhrasePrefix(m -> m
                    .field(IndexName.TITLE + IndexName.AUTOCOMPLETE_SUFFIX)
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
            // 자동 완성 검색
            boolQueryBuilder.should(s -> s
                .matchPhrasePrefix(m -> m
                    .field(IndexName.COMPANY + IndexName.AUTOCOMPLETE_SUFFIX)
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
            // 자동 완성 검색
            boolQueryBuilder.should(s -> s
                .matchPhrasePrefix(m -> m
                    .field(IndexName.REQUIRED_SKILLS + IndexName.AUTOCOMPLETE_SUFFIX)
                    .query(requestDto.getSearchTerm())
                    .boost(1.0f) // company 우선순위 1.5배 증가
                )
            );
        }

        return boolQueryBuilder;
    }
}
