package com.skytech.projectmanagement.comments.dto;

import com.skytech.projectmanagement.comments.entity.EntityType;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDTO {
    private UUID id;
    private Integer userId;
    private String fullName;
    private String avatar;
    private String email;
    private String body;
    private String entityId;
    private EntityType entityType;
    private Instant createdAt;
}