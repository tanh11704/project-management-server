package com.skytech.projectmanagement.notification.service.impl;

import com.skytech.projectmanagement.notification.dto.NotificationResponseDTO;
import com.skytech.projectmanagement.notification.service.NotificationBroadcastService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationBroadcastServiceImpl implements NotificationBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void broadcastNotification(Integer userId, NotificationResponseDTO notification) {
        String destination = "/user/" + userId + "/queue/notifications";
        messagingTemplate.convertAndSend(destination, notification);
        log.debug("Broadcasted thông báo đến người dùng {}: {}", userId, notification.getId());
    }

    @Override
    public void broadcastUnreadCount(Integer userId, Long unreadCount) {
        String destination = "/user/" + userId + "/queue/unread-count";
        messagingTemplate.convertAndSend(destination, unreadCount);
        log.debug("Broadcasted số lượng thông báo chưa đọc đến người dùng {}: {}", userId,
                unreadCount);
    }
}

