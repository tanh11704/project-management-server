package com.skytech.projectmanagement.Bug.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.skytech.projectmanagement.Bug.entity.BugSeverity;
import com.skytech.projectmanagement.Bug.entity.BugStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BugRequestDTO {
    @NotNull(message = "projectId là bắt buộc")
    @JsonProperty("projectId")
    private Integer projectId;

    @JsonProperty("originalTaskId")
    private Integer originalTaskId;

    @NotNull(message = "reporterId là bắt buộc")
    @JsonProperty("reporterId")
    private Integer reporterId;

    @NotNull(message = "title là bắt buộc")
    private String title;

    private String description;

    @NotNull(message = "status là bắt buộc")
    private BugStatus status;

    @NotNull(message = "severity là bắt buộc")
    private BugSeverity severity;
}
