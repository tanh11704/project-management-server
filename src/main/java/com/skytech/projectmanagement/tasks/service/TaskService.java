package com.skytech.projectmanagement.tasks.service;

import com.skytech.projectmanagement.tasks.dto.CreateTaskRequestDTO;
import com.skytech.projectmanagement.tasks.dto.TaskResponseDTO;
import com.skytech.projectmanagement.tasks.dto.UpdateTaskRequestDTO;
import com.skytech.projectmanagement.tasks.entity.Tasks;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface TaskService {
    TaskResponseDTO getTaskById(Integer taskId);
    TaskResponseDTO createTask(CreateTaskRequestDTO requestDTO, Authentication auth);
    TaskResponseDTO updateTask(Integer taskId, UpdateTaskRequestDTO requestDTO);
    void deleteTask(Integer taskId);
    List<TaskResponseDTO> getTasks(Integer projectId, Integer assigneeId, String status, String priority);
    Tasks getTaskEntityById(Integer taskId);

}
