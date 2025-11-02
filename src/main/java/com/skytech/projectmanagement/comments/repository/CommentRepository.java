package com.skytech.projectmanagement.comments.repository;

import com.skytech.projectmanagement.comments.entity.Comment;
import com.skytech.projectmanagement.comments.entity.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    @Query("SELECT c FROM Comment c WHERE c.entityId = :entityId AND c.entityType = :entityType ORDER BY c.createdAt DESC")
    List<Comment> findByEntity(@Param("entityId") String entityId, @Param("entityType") EntityType entityType);

}
