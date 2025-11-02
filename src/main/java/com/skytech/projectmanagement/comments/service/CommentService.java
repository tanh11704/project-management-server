package com.skytech.projectmanagement.comments.service;

import com.skytech.projectmanagement.comments.dto.CommentRequestDTO;
import com.skytech.projectmanagement.comments.dto.CommentResponseDTO;
import com.skytech.projectmanagement.comments.entity.EntityType;

import java.util.List;
import java.util.UUID;

public interface CommentService {
    List<CommentResponseDTO> getCommentsByEntity(EntityType entityType, String entityId);
    CommentResponseDTO createComment(CommentRequestDTO request, Integer currentUserId);
    CommentResponseDTO updateComment(UUID commentId, CommentRequestDTO request, Integer currentUserId);
    void deleteComment(UUID commentId, Integer currentUserId);
}
