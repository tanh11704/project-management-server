package com.skytech.projectmanagement.notification.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.skytech.projectmanagement.common.exception.ResourceNotFoundException;
import com.skytech.projectmanagement.notification.dto.NotificationCountDTO;
import com.skytech.projectmanagement.notification.dto.NotificationResponseDTO;
import com.skytech.projectmanagement.notification.entity.Notification;
import com.skytech.projectmanagement.notification.entity.NotificationType;
import com.skytech.projectmanagement.notification.mapper.NotificationMapper;
import com.skytech.projectmanagement.notification.repository.NotificationRepository;
import com.skytech.projectmanagement.notification.service.NotificationBroadcastService;
import com.skytech.projectmanagement.notification.service.NotificationService;
import com.skytech.projectmanagement.tasks.entity.TaskStatus;
import com.skytech.projectmanagement.tasks.entity.Tasks;
import com.skytech.projectmanagement.tasks.repository.TaskAssigneeRepository;
import com.skytech.projectmanagement.tasks.repository.TaskRepository;
import com.skytech.projectmanagement.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final TaskRepository taskRepository;
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final UserRepository userRepository;
    private final NotificationBroadcastService notificationBroadcastService;

    @Override
    public Page<NotificationResponseDTO> getUserNotifications(Integer userId, Pageable pageable) {
        Page<Notification> notifications =
                notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(notificationMapper::toDto);
    }

    @Override
    public Page<NotificationResponseDTO> getUnreadNotifications(Integer userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(notificationMapper::toDto);
    }

    @Override
    public NotificationCountDTO getUnreadCount(Integer userId) {
        Long count = notificationRepository.countUnreadByUserId(userId);
        return new NotificationCountDTO(count);
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId, Integer userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy thông báo với ID: " + notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Bạn không có quyền truy cập thông báo này");
        }

        notificationRepository.markAsRead(notificationId, true);

        // Broadcast updated unread count
        Long unreadCount = notificationRepository.countUnreadByUserId(userId);
        notificationBroadcastService.broadcastUnreadCount(userId, unreadCount);
    }

    @Override
    @Transactional
    public void markAllAsRead(Integer userId) {
        notificationRepository.markAllAsReadByUserId(userId);

        // Broadcast updated unread count
        notificationBroadcastService.broadcastUnreadCount(userId, 0L);
    }

    @Override
    @Transactional
    public void createNotification(Integer userId, Integer taskId, NotificationType type,
            String title, String message) {
        var user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTaskId(taskId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setIsRead(false);

        Notification saved = notificationRepository.save(notification);
        NotificationResponseDTO notificationDTO = notificationMapper.toDto(saved);

        // Broadcast notification via WebSocket
        notificationBroadcastService.broadcastNotification(userId, notificationDTO);

        // Broadcast updated unread count
        Long unreadCount = notificationRepository.countUnreadByUserId(userId);
        notificationBroadcastService.broadcastUnreadCount(userId, unreadCount);
    }

    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional
    public void checkAndCreateLateTaskNotifications() {
        log.info("Bắt đầu kiểm tra các task quá hạn để tạo thông báo");

        LocalDate today = LocalDate.now();

        // Use JPA Specification for efficient querying
        Specification<Tasks> lateTaskSpec = createLateTaskSpecification(today);
        List<Tasks> lateTasks = taskRepository.findAll(lateTaskSpec);

        log.info("Tìm thấy {} task quá hạn", lateTasks.size());

        for (Tasks task : lateTasks) {
            List<com.skytech.projectmanagement.tasks.entity.TaskAssignee> assignees =
                    taskAssigneeRepository.findByTaskId(task.getId());

            for (com.skytech.projectmanagement.tasks.entity.TaskAssignee assignee : assignees) {
                Integer userId = assignee.getUserId();

                // Check if notification already exists for this user and task created today
                List<Notification> existingNotifications =
                        notificationRepository.findByUserIdAndTaskIdAndType(userId, task.getId(),
                                NotificationType.LATE_DEADLINE_TASK);

                // Only create notification if no notification exists for today
                boolean notificationExistsToday = existingNotifications.stream().anyMatch(n -> {
                    LocalDate notificationDate =
                            n.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
                    return notificationDate.equals(today);
                });

                if (!notificationExistsToday) {
                    String title = String.format("Task '%s' đã quá hạn", task.getTitle());
                    long daysLate =
                            java.time.temporal.ChronoUnit.DAYS.between(task.getDueDate(), today);
                    String message = String.format(
                            "Task '%s' của dự án '%s' đã quá hạn %d ngày. Vui lòng hoàn thành sớm nhất có thể.",
                            task.getTitle(), task.getProject().getProjectName(), daysLate);

                    createNotification(userId, task.getId(), NotificationType.LATE_DEADLINE_TASK,
                            title, message);
                    log.info("Đã tạo thông báo cho user {} về task {} quá hạn", userId,
                            task.getId());
                }
            }
        }

        log.info("Hoàn thành kiểm tra và tạo thông báo cho task quá hạn");
    }

    @Override
    @Transactional
    public void checkTaskForOverdue(Integer taskId) {
        Tasks task = taskRepository.findById(taskId).orElse(null);

        if (task == null) {
            log.warn("Task với ID {} không tồn tại để kiểm tra quá hạn", taskId);
            return;
        }

        // Skip if task is already done or closed
        if (task.getStatus() == TaskStatus.DONE || task.getStatus() == TaskStatus.CLOSED) {
            return;
        }

        // Skip if task has no due date
        if (task.getDueDate() == null) {
            return;
        }

        LocalDate today = LocalDate.now();

        // Check if task is overdue
        if (task.getDueDate().isBefore(today)) {
            List<com.skytech.projectmanagement.tasks.entity.TaskAssignee> assignees =
                    taskAssigneeRepository.findByTaskId(task.getId());

            for (com.skytech.projectmanagement.tasks.entity.TaskAssignee assignee : assignees) {
                Integer userId = assignee.getUserId();

                // Check if notification already exists for this user and task created today
                List<Notification> existingNotifications =
                        notificationRepository.findByUserIdAndTaskIdAndType(userId, task.getId(),
                                NotificationType.LATE_DEADLINE_TASK);

                // Only create notification if no notification exists for today
                boolean notificationExistsToday = existingNotifications.stream().anyMatch(n -> {
                    LocalDate notificationDate =
                            n.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
                    return notificationDate.equals(today);
                });

                if (!notificationExistsToday) {
                    String title = String.format("Task '%s' đã quá hạn", task.getTitle());
                    long daysLate =
                            java.time.temporal.ChronoUnit.DAYS.between(task.getDueDate(), today);
                    String message = String.format(
                            "Task '%s' của dự án '%s' đã quá hạn %d ngày. Vui lòng hoàn thành sớm nhất có thể.",
                            task.getTitle(), task.getProject().getProjectName(), daysLate);

                    createNotification(userId, task.getId(), NotificationType.LATE_DEADLINE_TASK,
                            title, message);
                    log.info("Đã tạo thông báo cho user {} về task {} quá hạn", userId,
                            task.getId());
                }
            }
        }
    }

    private Specification<Tasks> createLateTaskSpecification(LocalDate today) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Task must have a due date
            predicates.add(cb.isNotNull(root.get("dueDate")));

            // Due date must be before today
            predicates.add(cb.lessThan(root.get("dueDate"), today));

            // Status must not be DONE or CLOSED
            predicates.add(cb.notEqual(root.get("status"), TaskStatus.DONE));
            predicates.add(cb.notEqual(root.get("status"), TaskStatus.CLOSED));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

