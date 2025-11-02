package com.skytech.projectmanagement.notification.service;

import com.skytech.projectmanagement.notification.dto.NotificationResponseDTO;

public interface NotificationBroadcastService {

    void broadcastNotification(Integer userId, NotificationResponseDTO notification);

    void broadcastUnreadCount(Integer userId, Long unreadCount);
}

