package com.skytech.projectmanagement.notification.dto;

import java.time.Instant;
import java.util.UUID;
import com.skytech.projectmanagement.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDTO {
    private UUID id;
    private Integer taskId;
    private NotificationType type;
    private String title;
    private String message;
    private Boolean isRead;
    private Instant createdAt;
}

