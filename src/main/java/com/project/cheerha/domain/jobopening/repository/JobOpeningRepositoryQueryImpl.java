package com.project.cheerha.domain.jobopening.repository;

import com.project.cheerha.domain.jobopening.dto.request.ReadJobOpeningRequestDto;
import com.project.cheerha.domain.jobopening.dto.response.QReadJobOpeningResponseDto;
import com.project.cheerha.domain.jobopening.dto.response.ReadJobOpeningResponseDto;
import com.project.cheerha.domain.jobopening.entity.EducationLevel;
import com.project.cheerha.domain.jobopening.entity.EmploymentType;
import com.project.cheerha.domain.jobopening.entity.RequiredSkills;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.project.cheerha.domain.jobopening.entity.QJobOpening.jobOpening;
import static com.project.cheerha.domain.keyword.entity.QJobOpeningKeyword.jobOpeningKeyword;
import static com.project.cheerha.domain.keyword.entity.QKeyword.keyword;

@Repository
@RequiredArgsConstructor
public class JobOpeningRepositoryQueryImpl implements JobOpeningRepositoryQuery {

    private final JPAQueryFactory queryFactory;

    /**
     * 주어진 검색 조건을 기반으로 채용 공고 목록을 조회합니다.
     *
     * 다양한 필터 조건을 적용해서 채용 공고 데이터를 가져오고, 해당 공고의 키워드(자격 요건)를 함께 조회합니다.
     *
     * @param requestDto 검색 및 필터링 조건
     * @param pageable 페이지 정보를 포함 (페이지 번호, 페이지 크기)
     * @return 필터링된 채용 공고 목록
     */
    @Override
    public Page<ReadJobOpeningResponseDto> findAllByCondition(
            ReadJobOpeningRequestDto requestDto, Pageable pageable
    ) {
        // 1. 기본 채용 공고 데이터 조회 (필터 조건 적용)
        List<ReadJobOpeningResponseDto> dtoList = queryFactory
            .select(new QReadJobOpeningResponseDto(
                jobOpening.id,
                jobOpening.title,
                jobOpening.company,
                jobOpening.location,
                jobOpening.salary,
                jobOpening.employmentType.stringValue(),
                jobOpening.educationLevel.stringValue(),
                jobOpening.jobOpeningUrl,
                jobOpening.minExperienceYears,
                jobOpening.maxExperienceYears,
                jobOpening.position,
                jobOpening.hiringStartAt,
                jobOpening.hiringEndAt,
                jobOpening.createdAt,
                jobOpening.viewCount
            ))
            .from(jobOpening)
            .leftJoin(jobOpening.jobOpeningKeywordList, jobOpeningKeyword) // 데이터와 키워드 테이블 조인
            .leftJoin(jobOpeningKeyword.keyword, keyword)
            .where(
                eqRequiredSkill(requestDto.getRequiredSkill()),
                eqLocation(requestDto.getLocation()),
                eqJobType(EmploymentType.toEnum(requestDto.getEmploymentType())),
                eqEducation(EducationLevel.toEnum(requestDto.getEducationLevel())),
                geoMinExperienceYears(requestDto.getMinExperienceYears()),
                leoMaxExperienceYears(requestDto.getMaxExperienceYears()),
                geoHiringStartPeriod(requestDto.getHiringStartAt()),
                leoHiringEndPeriod(requestDto.getHiringEndAt()),
                containsSearchTerm(requestDto.getSearchTerm())
            )
            .groupBy(jobOpening.id)
            .orderBy(jobOpening.createdAt.desc())  // 최신순 정렬
            .limit(pageable.getPageSize())
            .offset(pageable.getOffset())
            .fetch();

        // 2. 조회된 채용 공고 Id 목록 추출
        List<Long> dataIdList = dtoList.stream()
                .map(ReadJobOpeningResponseDto::getId)
                .toList();

        // 3. 해당 공고의 자격 요건 조회
        List<Tuple> requiredSkillTupleList = queryFactory
                .select(jobOpeningKeyword.jobOpening.id, keyword.name)
                .from(jobOpeningKeyword)
                .leftJoin(jobOpeningKeyword.keyword, keyword)
                .where(jobOpeningKeyword.jobOpening.id.in(dataIdList))
                .fetch();

        // 4. 채용 공고 Id별 자격 요건 리스트 매핑
        Map<Long, List<String>> requiredSkillMap = requiredSkillTupleList.stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(jobOpeningKeyword.jobOpening.id),
                        Collectors.mapping(tuple -> tuple.get(keyword.name), Collectors.toList())
                ));

        // 5. 각 Dto에 자격 요건 추가
        dtoList.forEach(dto -> {
            List<String> skills = requiredSkillMap.getOrDefault(dto.getId(), new ArrayList<>());
            dto.addRequiredSkills(new RequiredSkills(skills, true));
        });

        // 6. 결과 반환
        return new PageImpl<>(dtoList, pageable, dtoList.size());
    }

    /**
     * 조회수 기준으로 상위 100개의 인기 채용공고를 조회하고, 페이지네이션을 지원하는 메서드입니다.
     *
     * 이 메서드는 `jobopening` 테이블에서 조회수(viewCount)를 기준으로 상위 100개의 인기 채용공고를 내림차순으로 조회합니다.
     * 페이지네이션을 고려하여 사용자가 요청한 페이지에 맞는 데이터를 가져오며, 각 페이지당 표시되는 항목 수는 `pageable.getPageSize()`로 결정됩니다.
     * 또한, 데이터는 `offset`과 `limit`을 사용하여 최적화된 방식으로 쿼리됩니다.
     *
     * @param pageable 페이지 요청 정보 (페이지 번호, 페이지 크기 등)
     * @return 요청한 페이지의 인기 채용공고 목록을 포함하는 `Page` 객체
     *         - `dtoList`: 요청한 페이지에 해당하는 `ReadJobOpeningResponseDto` 객체 목록
     *         - `total`: 전체 인기 채용공고의 개수는 최대 100개로 제한
     */
    @Override
    public Page<ReadJobOpeningResponseDto> findTop100PopularJobOpenings(Pageable pageable) {
        List<ReadJobOpeningResponseDto> dtoList = queryFactory
                .select(new QReadJobOpeningResponseDto(
                        jobOpening.id,
                        jobOpening.title,
                        jobOpening.company,
                        jobOpening.location,
                        jobOpening.salary,
                        jobOpening.employmentType.stringValue(),
                        jobOpening.educationLevel.stringValue(),
                        jobOpening.jobOpeningUrl,
                        jobOpening.minExperienceYears,
                        jobOpening.maxExperienceYears,
                        jobOpening.position,
                        jobOpening.hiringStartAt,
                        jobOpening.hiringEndAt,
                        jobOpening.createdAt,
                        jobOpening.viewCount
                ))
                .from(jobOpening)
                .orderBy(jobOpening.viewCount.desc())
                .limit(100)
                .offset((long) pageable.getPageNumber() * pageable.getPageSize())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(dtoList, pageable, dtoList.size());
    }

    private BooleanExpression eqRequiredSkill(String requiredSkill) {
        return requiredSkill != null ? keyword.name.eq(requiredSkill) : Expressions.asBoolean(true).isTrue();
    }

    private BooleanExpression eqEducation(EducationLevel educationLevel) {
        return educationLevel != null ? jobOpening.educationLevel.eq(educationLevel) : Expressions.asBoolean(true).isTrue();
    }

    private BooleanExpression geoHiringStartPeriod(ZonedDateTime hiringStartAt) {
        return hiringStartAt != null ? jobOpening.hiringStartAt.goe(hiringStartAt) : Expressions.asBoolean(true).isTrue();
    }

    private BooleanExpression leoHiringEndPeriod(ZonedDateTime hiringEndAt) {
        return hiringEndAt != null ? jobOpening.hiringEndAt.loe(hiringEndAt) : Expressions.asBoolean(true).isTrue();
    }

    private BooleanExpression eqLocation(String location) {
        return location != null ? jobOpening.location.eq(location) : Expressions.asBoolean(true).isTrue();
    }

    private  BooleanExpression geoMinExperienceYears(Integer minExperienceYears) {
        return minExperienceYears != null ? jobOpening.minExperienceYears.goe(minExperienceYears) : Expressions.asBoolean(true).isTrue();
    }

    private BooleanExpression leoMaxExperienceYears(Integer maxExperienceYears) {
        return maxExperienceYears != null ? jobOpening.maxExperienceYears.loe(maxExperienceYears) : Expressions.asBoolean(true).isTrue();
    }

    private BooleanExpression eqJobType(EmploymentType employmentType) {
        return employmentType != null ? jobOpening.employmentType.eq(employmentType) : Expressions.asBoolean(true).isTrue();
    }

    private BooleanExpression containsSearchTerm(String searchTerm) {
        return searchTerm != null ? jobOpening.title.containsIgnoreCase(searchTerm) : Expressions.asBoolean(true).isTrue();
    }
}
