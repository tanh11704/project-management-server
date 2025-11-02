package com.skytech.projectmanagement.comments.mapper;

import com.skytech.projectmanagement.comments.dto.CommentResponseDTO;
import com.skytech.projectmanagement.comments.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "avatar", source = "user.avatar")
    @Mapping(target = "email", source = "user.email")
    CommentResponseDTO toResponse(Comment comment);
}
