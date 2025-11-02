package com.skytech.projectmanagement.Bug.dto;

import com.skytech.projectmanagement.Bug.entity.BugSeverity;
import com.skytech.projectmanagement.Bug.entity.BugStatus;
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
public class BugResponseDTO {
    private UUID id;
    private Integer projectId;
    private Integer reporterId;
    private Integer originalTaskId;
    private String title;
    private String description;
    private String status;  // ← String, không phải enum
    private String severity; // ← String
    private Instant createdAt;
    private Instant updatedAt;
}
