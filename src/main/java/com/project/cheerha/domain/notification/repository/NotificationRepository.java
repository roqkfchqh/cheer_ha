package com.project.cheerha.domain.notification.repository;

import com.project.cheerha.domain.notification.entity.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 아직 이메일이 발송되지 않은 Notification 목록 조회
     *
     * @return 이메일이 발송되지 않은 Notification 목록
     */
    List<Notification> findByIsEmailSentFalse();
}