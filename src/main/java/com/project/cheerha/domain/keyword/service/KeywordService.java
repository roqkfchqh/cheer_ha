package com.project.cheerha.domain.keyword.service;

import com.project.cheerha.common.exception.client.BadRequestException;
import com.project.cheerha.common.exception.client.ClientErrorCode;
import com.project.cheerha.domain.keyword.dto.response.KeywordCustomAgeResponseDto;
import com.project.cheerha.domain.keyword.dto.response.KeywordDto;
import com.project.cheerha.domain.keyword.dto.response.ReadKeywordResponseDto;
import com.project.cheerha.domain.keyword.entity.Keyword;
import com.project.cheerha.domain.keyword.repository.KeywordRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KeywordService {

    private final KeywordRepository keywordRepository;

    public ReadKeywordResponseDto readKeywords(String searchTerm) {
        List<Keyword> keywordList;

        if (searchTerm == null || searchTerm.isEmpty()) {
            keywordList = keywordRepository.findAll();
        } else {
            keywordList = keywordRepository.findByNameContaining(searchTerm);
        }

        List<KeywordDto> keywordDtoList = keywordList.stream()
            .map(keyword -> KeywordDto.toKeywordDto(
                    keyword.getId(),
                    keyword.getName()
                )
            )
            .toList();

        return ReadKeywordResponseDto.toDto(keywordDtoList);
    }

    /**
     * 커스텀 연령대 키워드 로직 중, 나이에 대한 예외처리가 진행됩니다.
     * @param minAge 최소 연령
     * @param maxAge 최대 연령
     * @return 커스텀 연령대 키워드 상위 10개 리스트
     */
    @Transactional(readOnly = true)
    public List<KeywordCustomAgeResponseDto> readTop10KeywordsByAgeBetween(int minAge, int maxAge)
    {
        // 최소나이가 최대나이보다 클 때 예외처리하는 로직 추가
        if (minAge > maxAge) {
            throw new BadRequestException(ClientErrorCode.MIN_AGE_EXCEEDS_MAX_AGE);
        }
        return keywordRepository.readTop10KeywordsByAgeGroup(minAge, maxAge);
    }
}