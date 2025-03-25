package com.project.cheerha.domain.userkeyword.respository;

import com.project.cheerha.domain.userkeyword.entity.UserKeyword;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserKeywordRepository extends JpaRepository<UserKeyword, Long> {

    boolean existsByUserIdAndKeywordId(Long userId, Long keywordId);

    boolean existsByUserIdAndId(Long userId, Long userKeywordId);

    List<UserKeyword> findByUserId(Long userId);
}