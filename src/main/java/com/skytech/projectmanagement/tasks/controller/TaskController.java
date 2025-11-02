package com.skytech.projectmanagement.tasks.controller;

import java.util.List;
import com.skytech.projectmanagement.attachment.dto.AttachmentResponse;
import com.skytech.projectmanagement.attachment.service.AttachmentService;
import com.skytech.projectmanagement.common.dto.SuccessResponse;
import com.skytech.projectmanagement.tasks.dto.CreateTaskRequestDTO;
import com.skytech.projectmanagement.tasks.dto.TaskResponseDTO;
import com.skytech.projectmanagement.tasks.dto.UpdateTaskRequestDTO;
import com.skytech.projectmanagement.tasks.service.TaskService;
import com.skytech.projectmanagement.user.entity.User;
import com.skytech.projectmanagement.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tasks-service/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final AttachmentService attachmentService;
    private final UserService userService;

    @PostMapping("/{taskId}/attachments")
    public ResponseEntity<SuccessResponse<AttachmentResponse>> uploadTaskAttachment(
            @PathVariable Integer taskId, @RequestParam("file") @Valid MultipartFile file) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // taskService.checkTaskAccess(taskId, authentication);

        User currentUser = userService.findUserByEmail(authentication.getName());
        Integer userId = currentUser.getId();

        AttachmentResponse attachmentDto =
                attachmentService.createAttachment(file, "TASK", taskId, userId);

        SuccessResponse<AttachmentResponse> response =
                SuccessResponse.of(attachmentDto, "Tải tệp lên thành công.");

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{taskId}/attachments")
    public ResponseEntity<SuccessResponse<List<AttachmentResponse>>> getTaskAttachments(
            @PathVariable Integer taskId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // taskService.checkTaskAccess(taskId, authentication);

        List<AttachmentResponse> attachments =
                attachmentService.getAttachmentsForEntity("TASK", taskId);

        SuccessResponse<List<AttachmentResponse>> response =
                SuccessResponse.of(attachments, "Lấy danh sách tệp đính kèm thành công.");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<TaskResponseDTO>> getTaskById(
            @PathVariable("id") Integer taskId) {
        TaskResponseDTO task = taskService.getTaskById(taskId);
        return ResponseEntity.ok(SuccessResponse.of(task, "Lấy task thành công"));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('PROJECT_MANAGE_ANY', 'PROJECT_MEMBER_MANAGE')")
    public ResponseEntity<SuccessResponse<TaskResponseDTO>> createTask(
            @Valid @RequestBody CreateTaskRequestDTO requestDTO, Authentication authentication) {

        TaskResponseDTO createdTask = taskService.createTask(requestDTO, authentication);

        return ResponseEntity.ok(SuccessResponse.of(createdTask, "Tạo task thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<TaskResponseDTO>> updateTask(
            @PathVariable("id") Integer taskId,
            @Valid @RequestBody UpdateTaskRequestDTO requestDTO) {
        TaskResponseDTO updatedTask = taskService.updateTask(taskId, requestDTO);
        return ResponseEntity.ok(SuccessResponse.of(updatedTask, "Cập nhật task thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PROJECT_MANAGE_ANY', 'PROJECT_MEMBER_MANAGE')")
    public ResponseEntity<SuccessResponse<Void>> deleteTask(@PathVariable("id") Integer taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.ok(SuccessResponse.of(null, "Xóa task thành công"));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PROJECT_MANAGE_ANY', 'PROJECT_MEMBER_MANAGE')")
    public ResponseEntity<SuccessResponse<List<TaskResponseDTO>>> getTasks(
            @RequestParam(required = false) Integer projectId,
            @RequestParam(required = false) Integer assigneeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority) {
        List<TaskResponseDTO> tasks = taskService.getTasks(projectId, assigneeId, status, priority);
        return ResponseEntity.ok(SuccessResponse.of(tasks, "Lấy danh sách task thành công"));
    }

}