package com.project.cheerha.domain.notification.service;

import com.project.cheerha.domain.notification.dto.NotificationDto;
import com.project.cheerha.domain.notification.entity.Notification;
import com.project.cheerha.domain.notification.repository.NotificationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createNotification(List<NotificationDto> notificationDtoList) {
        List<Notification> notificationList = notificationDtoList.stream()
            .map(dto -> Notification.toEntity
                (dto.email(),
                    dto.jobOpeningUrl()
                )).toList();
        if (!notificationList.isEmpty()) {
            notificationRepository.saveAll(notificationList);
        }
    }
}