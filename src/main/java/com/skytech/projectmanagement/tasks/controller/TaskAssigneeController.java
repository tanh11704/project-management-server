package com.skytech.projectmanagement.tasks.controller;

import com.skytech.projectmanagement.common.dto.SuccessResponse;
import com.skytech.projectmanagement.tasks.dto.TaskAssigneeResponseDTO;
import com.skytech.projectmanagement.tasks.entity.TaskAssignee;
import com.skytech.projectmanagement.tasks.mapper.TaskAssigneeMapper;
import com.skytech.projectmanagement.tasks.service.TaskAssigneeService;
import com.skytech.projectmanagement.teams.mapper.TeamMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks-service/v1/tasks/{taskId}/assignees")
@RequiredArgsConstructor
public class TaskAssigneeController {
    private final TaskAssigneeService taskAssigneeService;

    @PostMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('PROJECT_MANAGE_ANY', 'PROJECT_MEMBER_MANAGE')")
    public ResponseEntity<?> assignUserToTask(@PathVariable Integer taskId,
                                              @PathVariable Integer userId){
        TaskAssigneeResponseDTO dto = taskAssigneeService.assignUserToTask(taskId, userId);
        return ResponseEntity.ok(SuccessResponse.of(dto, "Thêm user vào task thành công."));
    }
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('PROJECT_MANAGE_ANY', 'PROJECT_MEMBER_MANAGE')")
    public ResponseEntity<?> unassignUserFromTask(@PathVariable Integer taskId,
                                                  @PathVariable Integer userId) {
        taskAssigneeService.unassignUserFromTask(taskId, userId);
        return ResponseEntity.ok(SuccessResponse.of(null, "Bỏ gán user khỏi task thành công."));
    }
    @GetMapping
    @PreAuthorize("hasAnyAuthority('PROJECT_MANAGE_ANY', 'PROJECT_MEMBER_MANAGE')")
    public ResponseEntity<?> getAssigneesByTask(@PathVariable Integer taskId) {
        var list = taskAssigneeService.getAssigneesByTask(taskId);
        return ResponseEntity.ok(SuccessResponse.of(list, "Lấy danh sách assignee thành công."));
    }
}
