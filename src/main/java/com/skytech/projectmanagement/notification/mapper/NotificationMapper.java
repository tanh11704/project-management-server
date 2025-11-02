package com.skytech.projectmanagement.notification.mapper;

import com.skytech.projectmanagement.notification.dto.NotificationResponseDTO;
import com.skytech.projectmanagement.notification.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponseDTO toDto(Notification notification);
}

