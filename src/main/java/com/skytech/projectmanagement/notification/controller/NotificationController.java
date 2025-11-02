package com.skytech.projectmanagement.notification.controller;

import java.util.UUID;
import com.skytech.projectmanagement.common.dto.SuccessResponse;
import com.skytech.projectmanagement.notification.dto.NotificationCountDTO;
import com.skytech.projectmanagement.notification.dto.NotificationResponseDTO;
import com.skytech.projectmanagement.notification.service.NotificationService;
import com.skytech.projectmanagement.user.entity.User;
import com.skytech.projectmanagement.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notification-service/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<SuccessResponse<Page<NotificationResponseDTO>>> getNotifications(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        User currentUser = userService.findUserByEmail(authentication.getName());
        Pageable pageable = PageRequest.of(page, size);

        Page<NotificationResponseDTO> notifications =
                notificationService.getUserNotifications(currentUser.getId(), pageable);

        return ResponseEntity
                .ok(SuccessResponse.of(notifications, "Lấy danh sách thông báo thành công"));
    }

    @GetMapping("/unread")
    public ResponseEntity<SuccessResponse<Page<NotificationResponseDTO>>> getUnreadNotifications(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        User currentUser = userService.findUserByEmail(authentication.getName());
        Pageable pageable = PageRequest.of(page, size);

        Page<NotificationResponseDTO> notifications =
                notificationService.getUnreadNotifications(currentUser.getId(), pageable);

        return ResponseEntity.ok(
                SuccessResponse.of(notifications, "Lấy danh sách thông báo chưa đọc thành công"));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<SuccessResponse<NotificationCountDTO>> getUnreadCount(
            Authentication authentication) {
        User currentUser = userService.findUserByEmail(authentication.getName());
        NotificationCountDTO count = notificationService.getUnreadCount(currentUser.getId());

        return ResponseEntity
                .ok(SuccessResponse.of(count, "Lấy số lượng thông báo chưa đọc thành công"));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<SuccessResponse<Void>> markAsRead(@PathVariable UUID notificationId,
            Authentication authentication) {

        User currentUser = userService.findUserByEmail(authentication.getName());
        notificationService.markAsRead(notificationId, currentUser.getId());

        return ResponseEntity.ok(SuccessResponse.of(null, "Đánh dấu đã đọc thành công"));
    }

    @PutMapping("/read-all")
    public ResponseEntity<SuccessResponse<Void>> markAllAsRead(Authentication authentication) {
        User currentUser = userService.findUserByEmail(authentication.getName());
        notificationService.markAllAsRead(currentUser.getId());

        return ResponseEntity.ok(SuccessResponse.of(null, "Đánh dấu tất cả đã đọc thành công"));
    }
}

