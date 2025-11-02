package com.skytech.projectmanagement.tasks.service.impl;

import java.util.ArrayList;
import java.util.List;
import com.skytech.projectmanagement.common.exception.ResourceNotFoundException;
import com.skytech.projectmanagement.notification.service.NotificationService;
import com.skytech.projectmanagement.project.entity.Project;
import com.skytech.projectmanagement.project.repository.ProjectRepository;
import com.skytech.projectmanagement.tasks.dto.CreateTaskRequestDTO;
import com.skytech.projectmanagement.tasks.dto.TaskResponseDTO;
import com.skytech.projectmanagement.tasks.dto.UpdateTaskRequestDTO;
import com.skytech.projectmanagement.tasks.entity.TaskStatus;
import com.skytech.projectmanagement.tasks.entity.Tasks;
import com.skytech.projectmanagement.tasks.mapper.TaskMapper;
import com.skytech.projectmanagement.tasks.repository.TaskRepository;
import com.skytech.projectmanagement.tasks.service.TaskService;
import com.skytech.projectmanagement.user.entity.User;
import com.skytech.projectmanagement.user.repository.UserRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    public TaskResponseDTO getTaskById(Integer taskId) {
        Tasks task = taskRepository.findById(taskId).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy task với ID: " + taskId));

        return taskMapper.toDto(task);
    }

    @Override
    public TaskResponseDTO createTask(CreateTaskRequestDTO requestDTO, Authentication auth) {
        User currentUser = userRepository.findByEmail(auth.getName()).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy người dùng hiện tại"));

        Project project = projectRepository.findById(requestDTO.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy project với ID: " + requestDTO.getProjectId()));

        Tasks task = taskMapper.toEntity(requestDTO);
        task.setCreator(currentUser);
        task.setProject(project);

        if (requestDTO.getParentTaskId() != null) {
            Tasks parent = taskRepository.findById(requestDTO.getParentTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy task cha"));
            task.setParentTask(parent);
        }

        Tasks saved = taskRepository.save(task);

        // Check if the newly created task is already overdue
        if (saved.getDueDate() != null && saved.getDueDate().isBefore(java.time.LocalDate.now())) {
            notificationService.checkTaskForOverdue(saved.getId());
        }

        return taskMapper.toDto(saved);
    }

    @Override
    public TaskResponseDTO updateTask(Integer taskId, UpdateTaskRequestDTO requestDTO) {
        Tasks existingTask = taskRepository.findById(taskId).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy task với ID: " + taskId));

        // Store original values for comparison
        java.time.LocalDate originalDueDate = existingTask.getDueDate();
        TaskStatus originalStatus = existingTask.getStatus();

        taskMapper.updateEntityFromDTO(requestDTO, existingTask);
        Tasks updated = taskRepository.save(existingTask);

        // Check if we need to validate overdue status:
        // 1. Due date was changed (added or modified)
        // 2. Status changed from DONE/CLOSED back to active status
        boolean dueDateChanged = (originalDueDate == null && updated.getDueDate() != null)
                || (originalDueDate != null && updated.getDueDate() != null
                && !originalDueDate.equals(updated.getDueDate()));

        boolean statusChangedToActive =
                (originalStatus == TaskStatus.DONE || originalStatus == TaskStatus.CLOSED)
                        && updated.getStatus() != TaskStatus.DONE
                        && updated.getStatus() != TaskStatus.CLOSED;

        // Only check if a relevant change occurred
        if (dueDateChanged || statusChangedToActive) {
            notificationService.checkTaskForOverdue(updated.getId());
        }

        return taskMapper.toDto(updated);
    }

    @Override
    public void deleteTask(Integer taskId) {
        Tasks existingTask = taskRepository.findById(taskId).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy task với ID: " + taskId));
        taskRepository.delete(existingTask);
    }

    @Override
    public List<TaskResponseDTO> getTasks(Integer projectId, Integer assigneeId, String status,
                                          String priority) {
        var spec = filter(projectId != null ? projectId.intValue() : null,
                assigneeId != null ? assigneeId.intValue() : null, status, priority);
        return taskRepository.findAll(spec).stream().map(taskMapper::toDto).toList();
    }


    private Specification<Tasks> filter(Integer projectId, Integer assigneeId, String status,
                                        String priority) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (projectId != null)
                predicates.add(cb.equal(root.get("project").get("id"), projectId));

            if (assigneeId != null) {
                Join<Object, Object> assigneeJoin = root.join("assignees"); // join bảng trung gian
                predicates.add(cb.equal(assigneeJoin.get("id"), assigneeId));
            }

            if (status != null)
                predicates.add(cb.equal(root.get("status"), status));

            if (priority != null)
                predicates.add(cb.equal(root.get("priority"), priority));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}