package com.skytech.projectmanagement.tasks.mapper;

import com.skytech.projectmanagement.tasks.dto.CreateTaskRequestDTO;
import com.skytech.projectmanagement.tasks.dto.TaskResponseDTO;
import com.skytech.projectmanagement.tasks.dto.UpdateTaskRequestDTO;
import com.skytech.projectmanagement.tasks.entity.Tasks;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.projectName")
    @Mapping(target = "creatorId", source = "creator.id")
    @Mapping(target = "creatorName", source = "creator.fullName")
    @Mapping(target = "parentTaskId", source = "parentTask.id")
    TaskResponseDTO toDto(Tasks tasks);

    // Map DTO -> Entity (chỉ map field cơ bản)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "parentTask", ignore = true)
    Tasks toEntity(CreateTaskRequestDTO createTaskRequestDTO);

    // Cập nhật task
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(UpdateTaskRequestDTO updateTaskRequestDTO, @MappingTarget Tasks task);
}
