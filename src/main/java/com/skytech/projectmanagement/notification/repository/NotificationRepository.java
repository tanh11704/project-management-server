package com.skytech.projectmanagement.notification.repository;

import java.util.List;
import java.util.UUID;
import com.skytech.projectmanagement.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);

    Page<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Integer userId,
            Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false")
    Long countUnreadByUserId(@Param("userId") Integer userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId")
    void markAllAsReadByUserId(@Param("userId") Integer userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = :isRead WHERE n.id = :notificationId")
    void markAsRead(@Param("notificationId") UUID notificationId, @Param("isRead") Boolean isRead);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.taskId = :taskId AND n.type = :type")
    List<Notification> findByUserIdAndTaskIdAndType(@Param("userId") Integer userId,
            @Param("taskId") Integer taskId,
            @Param("type") com.skytech.projectmanagement.notification.entity.NotificationType type);
}

