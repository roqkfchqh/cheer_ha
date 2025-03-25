package com.project.cheerha.domain.searchhistory.service;

import com.project.cheerha.common.repository.KeyValueCommandRepository;
import com.project.cheerha.common.repository.KeyValueQueryRepository;
import com.project.cheerha.domain.searchhistory.entity.SearchHistory;
import com.project.cheerha.domain.searchhistory.repository.SearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final KeyValueCommandRepository keyValueCommandRepository;
    private final KeyValueQueryRepository keyValueQueryRepository;

    private static final int MAX_SEARCH_HISTORY_SIZE = 10; // 최대 저장 개수
    private static final long EXPIRATION_TIME = 3600; // 1시간(초 단위)

    /**
     * 사용자의 검색어를 Redis에 저장하는 메서드입니다.
     *
     * 검색어는 저장된 시간을 기준으로 정렬됩니다.
     * 최대 10개까지 저장되, 초과 시 가장 오래된 검색어가 삭제됩니다.
     * 검색 기록은 일정 시간이 지나면 자동으로 만료됩니다.
     *
     * @param userId 검색어를 저장할 유저의 Id
     * @param searchTerm 사용자가 입력한 검색어
     */
    public void saveSearchTerm(Long userId, String searchTerm) {
        String key = getKey(userId);
        long timestamp = System.currentTimeMillis();
        if(!searchTerm.isBlank()) {
            keyValueCommandRepository.addToZSet(key, searchTerm, timestamp);
        }
        Long size = keyValueQueryRepository.getZSetCard(key);
        if (size != null && size > MAX_SEARCH_HISTORY_SIZE) {
            keyValueCommandRepository.removeFromZSetRange(key, 0, size - MAX_SEARCH_HISTORY_SIZE - 1);
        }
        keyValueCommandRepository.expireValue(key, EXPIRATION_TIME, TimeUnit.SECONDS);
    }

    /**
     * 사용자의 최근 검색어 목록을 조회하는 메서드입니다.
     *
     * Redis에 저장된 검색어 목록을 조회하며, 최대 10개까지 반환 가능합니다.
     * Redis에 검색어가 없을 경우, DB에서 상위 10개를 조회하여 Redis에 저장합니다.
     * DB에서 가져온 검색어는 저장된 순서를 유지하기 위해 정렬 후 Redis에 역순으로 저장됩니다.
     *
     * @param userId 검색어를 조회할 유저의 Id
     * @return 최근 검색한 검색어 목록 (10개)
     */
    public List<String> getRecentSearchTerms(Long userId) {
        String key = getKey(userId);

        Set<String> searchTermSet = keyValueQueryRepository.getZSetReverseRange(key, 0, 9);
        if (searchTermSet == null || searchTermSet.isEmpty()) {
            return fetchSearchTermListFromDatabase(userId);
        }

        return new ArrayList<>(searchTermSet);
    }

    /**
     * 사용자의 최근 검색 기록을 DB에서 조회하여 Redis에 캐싱한 후 반환한다.
     *
     * Redis에 검색 기록이 없는 경우, 이 메서드를 호출하여 DB에서 데이터를 가져온다.
     * 가져온 데이터를 Redis에 저장하여 이후 빠른 검색을 가능하게 한다.
     */
    private List<String> fetchSearchTermListFromDatabase(Long userId) {
        List<String> searchTermListInDatabase = searchHistoryRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .sorted(Comparator.comparing(SearchHistory::getCreatedAt))
            .map(SearchHistory::getName)
            .collect(Collectors.toList());

        searchTermListInDatabase.forEach(term -> saveSearchTerm(userId, term));
        Collections.reverse(searchTermListInDatabase);

        return searchTermListInDatabase;
    }

    private String getKey(Long userId) {
        return "user:" + userId + ":search_history";
    }
}
