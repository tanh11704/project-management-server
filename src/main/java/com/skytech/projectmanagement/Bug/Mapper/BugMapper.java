package com.skytech.projectmanagement.Bug.Mapper;

import com.skytech.projectmanagement.Bug.dto.BugRequestDTO;
import com.skytech.projectmanagement.Bug.dto.BugResponseDTO;
import com.skytech.projectmanagement.Bug.entity.Bug;
import com.skytech.projectmanagement.project.entity.Project;
import com.skytech.projectmanagement.tasks.entity.Tasks;
import com.skytech.projectmanagement.user.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BugMapper {

    @Mapping(target = "project", ignore = true)
    @Mapping(target = "reporter", ignore = true)
    @Mapping(target = "originalTask", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    Bug toEntity(BugRequestDTO dto);

    default BugResponseDTO toResponseDto(Bug bug) {
        if (bug == null) return null;

        BugResponseDTO dto = new BugResponseDTO();
        dto.setId(bug.getId());
        dto.setProjectId(bug.getProject() != null ? bug.getProject().getId() : null);
        dto.setReporterId(bug.getReporter() != null ? bug.getReporter().getId() : null);
        dto.setOriginalTaskId(bug.getOriginalTask() != null ? bug.getOriginalTask().getId() : null);
        dto.setTitle(bug.getTitle());
        dto.setDescription(bug.getDescription());
        dto.setStatus(bug.getStatus() != null ? bug.getStatus().name() : null);
        dto.setSeverity(bug.getSeverity() != null ? bug.getSeverity().name() : null);
        dto.setCreatedAt(bug.getCreatedAt());
        dto.setUpdatedAt(bug.getUpdatedAt());
        return dto;
    }
}
