package com.skytech.projectmanagement.comments.controller;

import com.skytech.projectmanagement.auth.security.JwtTokenProvider;
import com.skytech.projectmanagement.comments.dto.CommentRequestDTO;
import com.skytech.projectmanagement.comments.dto.CommentResponseDTO;
import com.skytech.projectmanagement.comments.entity.EntityType;
import com.skytech.projectmanagement.comments.service.CommentService;
import com.skytech.projectmanagement.common.dto.SuccessResponse;
import com.skytech.projectmanagement.user.entity.User;
import com.skytech.projectmanagement.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/comment-service/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    @GetMapping
    @PreAuthorize("hasAnyAuthority('PROJECT_MANAGE_ANY', 'PROJECT_MEMBER_MANAGE')")
    public ResponseEntity<?> getCommentsByEntity(
            @RequestParam EntityType entityType,
            @RequestParam String entityId
    ) {
        List<CommentResponseDTO> comments = commentService.getCommentsByEntity(entityType, entityId);
        return ResponseEntity.ok(SuccessResponse.of(comments, "Lấy danh sách bình luận thành công"));
    }

    @PostMapping
    public ResponseEntity<?> createComment(
            @Valid @RequestBody CommentRequestDTO request,
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.replace("Bearer ", "");
        // Lấy email từ token
        String email = jwtTokenProvider.getEmail(token);

        User user = userService.findUserByEmail(email);
        Integer currentUserId = user.getId();

        CommentResponseDTO response = commentService.createComment(request, currentUserId);

        return ResponseEntity.ok(SuccessResponse.of(response, "Tạo bình luận thành công"));
    }


    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable UUID commentId,
            @Valid @RequestBody CommentRequestDTO request,
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtTokenProvider.getEmail(token);
        User user = userService.findUserByEmail(email);
        Integer currentUserId = user.getId();

        CommentResponseDTO updated = commentService.updateComment(commentId, request, currentUserId);
        return ResponseEntity.ok(SuccessResponse.of(updated, "Cập nhật bình luận thành công"));
    }


    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasAnyAuthority('PROJECT_MANAGE_ANY', 'PROJECT_MEMBER_MANAGE')")
    public ResponseEntity<?> deleteComment(
            @PathVariable UUID commentId,
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtTokenProvider.getEmail(token);
        User user = userService.findUserByEmail(email);
        Integer currentUserId = user.getId();
        commentService.deleteComment(commentId, currentUserId);
        return ResponseEntity.ok(SuccessResponse.of(null,"Xóa bình luận thành công"));
    }
}
