package com.project.cheerha.domain.jobopening.service;

import com.project.cheerha.common.dto.ViewCountResponseDto;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.project.cheerha.common.repository.KeyValueCommandRepository;
import com.project.cheerha.common.repository.KeyValueQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ViewCountManager {

    private final KeyValueQueryRepository keyValueQueryRepository;
    private final KeyValueCommandRepository keyValueCommandRepository;

    // 조회수 키의 기본 Prefix
    private static final String VIEW_COUNT_KEY_PREFIX = "viewCount:";

    /**
     * 특정 채용공고의 조회수를 증가시키는 메서드입니다.
     * @param jobOpeningId 조회수를 증가시킬 채용공고 ID
     */
    public void increaseViewCount(Long jobOpeningId) {
        String key = getKey(jobOpeningId);
        // 키가 없으면 기본값 0 설정
        if (keyValueQueryRepository.getValue(key) == null) {
            keyValueCommandRepository.setValue(key, "0", Duration.ofMinutes(30));  // 30분 TTL 설정
        }

        keyValueCommandRepository.incrementValue(key);
    }

    /**
     * viewCount 단일 값
     * @param jobOpeningId
     * @return 채용공고 id에 해당하는 viewCount 값
     */
    public long getViewCount(Long jobOpeningId) {
        String key = getKey(jobOpeningId);
        String count = keyValueQueryRepository.getValue(key);
        return count == null ? 0 : Long.parseLong(count);
    }

    /**
     * 조회 수 동기화 후 RDB에 값이 반영된 조회 수는 Redis에서 삭제해주는 메서드
     * @param jobOpeningId 키를 삭제할 채용공고 Id 값
     */
    public void resetViewCount(Long jobOpeningId) {
        String key = getKey(jobOpeningId);
        keyValueCommandRepository.removeValue(key);
    }

    /**
     * 채용공고의 조회수를 전체 가져오는 메서드입니다.
     * 동기화를 위한 메서드
     * @return 현재 저장된 조회수 전부
     */
    public List<ViewCountResponseDto> findAllViewCount() {
        Set<String> keys = keyValueQueryRepository.getKeys(VIEW_COUNT_KEY_PREFIX + "*"); // 모든 조회수 키 찾기
        List<ViewCountResponseDto> result = new ArrayList<>();

        for(String key : keys) {
            Long jobOpeningId = Long.parseLong(key.replace(VIEW_COUNT_KEY_PREFIX, ""));
            Long count = Long.valueOf(Objects.requireNonNull(keyValueQueryRepository.getValue(key)));
            result.add(new ViewCountResponseDto(jobOpeningId, count));
        }
        return result;
    }

    private String getKey(Long jobOpeningId) {
        return VIEW_COUNT_KEY_PREFIX + jobOpeningId;
    }
}

