package com.skytech.projectmanagement.Bug.Mapper;

import com.skytech.projectmanagement.Bug.dto.BugAssigneesDTO;
import com.skytech.projectmanagement.user.dto.UserResponse;
import com.skytech.projectmanagement.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BugAssigneesMapper {

    @Mapping(target = "userId", source = "id")
    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "email", source = "email")
    BugAssigneesDTO toDto(UserResponse user);
}