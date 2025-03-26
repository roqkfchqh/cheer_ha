package com.project.cheerha.domain.bookmark.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.cheerha.common.repository.KeyHashRepository;
import com.project.cheerha.domain.bookmark.dto.response.ReadBookmarkResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class BookmarkCacheService {

    private final KeyHashRepository keyHashRepository;
    private final ObjectMapper objectMapper;

    // 캐시에서 북마크를 가져오는 메서드
    public List<ReadBookmarkResponseDto> getAllBookmarksFromCache(Long userId) {
        Set<Object> cachedBookmarkIdsSet = keyHashRepository.getKeys("user:" + userId + ":bookmarks");
        List<Object> cachedBookmarkObjects = new ArrayList<>(cachedBookmarkIdsSet);
        if (!cachedBookmarkObjects.isEmpty()) {
            List<Object> rawCacheData = keyHashRepository.multiGet("user:" + userId + ":bookmarks", cachedBookmarkObjects);
            List<ReadBookmarkResponseDto> dtoList = new ArrayList<>();

            for (Object rawData : rawCacheData) {
                if (rawData instanceof LinkedHashMap) {
                    //json -> dto
                    ReadBookmarkResponseDto dto = objectMapper.convertValue(rawData, ReadBookmarkResponseDto.class);
                    dtoList.add(dto);
                } else if (rawData instanceof ReadBookmarkResponseDto) {
                    dtoList.add((ReadBookmarkResponseDto) rawData);
                }
            }
            return dtoList;
        }
        return new ArrayList<>();
    }

    // 북마크 추가 후 캐시 갱신하는 메서드
    public void updateCacheOnBookmarkAdd(Long userId, ReadBookmarkResponseDto dto) {
        keyHashRepository.putValue("user:" + userId.toString() + ":bookmarks", dto.id().toString(), dto);
    }

    // 캐시에서 북마크 삭제하는 메서드
    public void deleteBookmarkFromCache(Long userId, Long jobOpeningId) {
        keyHashRepository.deleteValue("user:" + userId.toString() + ":bookmarks", jobOpeningId.toString());
    }
}
