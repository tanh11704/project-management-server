package com.skytech.projectmanagement.notification.service;

import java.util.UUID;
import com.skytech.projectmanagement.notification.dto.NotificationCountDTO;
import com.skytech.projectmanagement.notification.dto.NotificationResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    Page<NotificationResponseDTO> getUserNotifications(Integer userId, Pageable pageable);

    Page<NotificationResponseDTO> getUnreadNotifications(Integer userId, Pageable pageable);

    NotificationCountDTO getUnreadCount(Integer userId);

    void markAsRead(UUID notificationId, Integer userId);

    void markAllAsRead(Integer userId);

    void createNotification(Integer userId, Integer taskId,
            com.skytech.projectmanagement.notification.entity.NotificationType type, String title,
            String message);

    void checkAndCreateLateTaskNotifications();

    void checkTaskForOverdue(Integer taskId);
}

