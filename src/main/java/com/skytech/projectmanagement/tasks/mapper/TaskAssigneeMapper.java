package com.skytech.projectmanagement.tasks.mapper;

import com.skytech.projectmanagement.tasks.dto.TaskAssigneeRequestDTO;
import com.skytech.projectmanagement.tasks.dto.TaskAssigneeResponseDTO;
import com.skytech.projectmanagement.tasks.entity.TaskAssignee;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TaskAssigneeMapper {

    TaskAssignee toEntity(TaskAssigneeRequestDTO dto);

    TaskAssigneeResponseDTO toDto(TaskAssignee entity);
}
