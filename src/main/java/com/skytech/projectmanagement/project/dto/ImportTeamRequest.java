package com.skytech.projectmanagement.project.dto;

import java.util.UUID;
import com.skytech.projectmanagement.project.entity.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record ImportTeamRequest(@NotNull(message = "Team ID không được để trống") UUID teamId,
        @NotNull(message = "Vai trò mặc định không được để trống") ProjectRole defaultRole) {
}
