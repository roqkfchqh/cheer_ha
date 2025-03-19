package com.project.cheerha.domain.user.service;

import com.project.cheerha.domain.user.dto.response.ReadUserResponseDto;
import com.project.cheerha.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserFindByService userFindByService;

    public ReadUserResponseDto readUser(Long userId) {
        User user = userFindByService.findById(userId);
        return ReadUserResponseDto.toDto(user.getEmail(), user.getName(), user.getCareer(), user.getAge(), user.isNotificationEnabled());
    }
}
