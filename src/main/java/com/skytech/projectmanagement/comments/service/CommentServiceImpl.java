package com.skytech.projectmanagement.comments.service;

import com.skytech.projectmanagement.Bug.service.BugService;
import com.skytech.projectmanagement.comments.dto.CommentRequestDTO;
import com.skytech.projectmanagement.comments.dto.CommentResponseDTO;
import com.skytech.projectmanagement.comments.entity.Comment;
import com.skytech.projectmanagement.comments.entity.EntityType;
import com.skytech.projectmanagement.comments.mapper.CommentMapper;
import com.skytech.projectmanagement.comments.repository.CommentRepository;
import com.skytech.projectmanagement.common.exception.ResourceNotFoundException;
import com.skytech.projectmanagement.common.exception.ValidationException;
import com.skytech.projectmanagement.project.service.ProjectService;
import com.skytech.projectmanagement.tasks.service.TaskService;
import com.skytech.projectmanagement.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final TaskService taskService;
    private final BugService bugService;
    private final CommentMapper commentMapper;

    @Override
    public List<CommentResponseDTO> getCommentsByEntity(EntityType entityType, String entityId) {
        // Validate entityId format trước
        validateEntityIdFormat(entityType, entityId);

        var comments = commentRepository.findByEntity(entityId, entityType);
        return comments.stream()
                .map(commentMapper::toResponse)
                .toList();
    }

    @Override
    public CommentResponseDTO createComment(CommentRequestDTO request, Integer currentUserId) {
        var user = userService.getUserEntityById(currentUserId);

        // Validate entity tồn tại
        validateEntity(request.getEntityType(), request.getEntityId());

        var comment = new Comment();
        comment.setUser(user);
        comment.setBody(request.getBody());
        comment.setEntityId(request.getEntityId());
        comment.setEntityType(request.getEntityType());

        var saved = commentRepository.save(comment);
        return commentMapper.toResponse(saved);
    }

    @Override
    public CommentResponseDTO updateComment(UUID commentId, CommentRequestDTO request, Integer currentUserId) {
        var comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bình luận"));

        if (!comment.getUser().getId().equals(currentUserId)) {
            throw new ValidationException("Bạn không có quyền chỉnh sửa bình luận này");
        }

        comment.setBody(request.getBody());
        var updated = commentRepository.save(comment);
        return commentMapper.toResponse(updated);
    }
    @Override
    public void deleteComment(UUID commentId, Integer currentUserId) {
        var comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bình luận"));

        commentRepository.delete(comment);
    }


    private void validateEntityIdFormat(EntityType entityType, String entityId) {
        try {
            if (entityType == EntityType.BUG) {
                UUID.fromString(entityId);
            } else {
                Integer.parseInt(entityId);
            }
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("entityId không hợp lệ cho loại " + entityType);
        }
    }

    private void validateEntity(EntityType entityType, String entityId) {
        try {
            switch (entityType) {
                case BUG -> bugService.getBugById(UUID.fromString(entityId));
                case TASK -> taskService.getTaskById(Integer.valueOf(entityId));
            }
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("entityId không hợp lệ cho loại " + entityType);
        }
    }
}
