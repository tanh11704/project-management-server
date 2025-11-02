package com.skytech.projectmanagement.Bug.service;

import com.skytech.projectmanagement.Bug.Mapper.BugMapper;
import com.skytech.projectmanagement.Bug.dto.BugRequestDTO;
import com.skytech.projectmanagement.Bug.dto.BugResponseDTO;
import com.skytech.projectmanagement.Bug.entity.Bug;
import com.skytech.projectmanagement.Bug.repository.BugRepository;
import com.skytech.projectmanagement.common.exception.ResourceNotFoundException;
import com.skytech.projectmanagement.project.entity.Project;
import com.skytech.projectmanagement.project.repository.ProjectRepository;
import com.skytech.projectmanagement.project.service.ProjectService;
import com.skytech.projectmanagement.tasks.entity.Tasks;
import com.skytech.projectmanagement.tasks.repository.TaskRepository;
import com.skytech.projectmanagement.tasks.service.TaskService;
import com.skytech.projectmanagement.user.entity.User;
import com.skytech.projectmanagement.user.repository.UserRepository;
import com.skytech.projectmanagement.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BugServiceImpl implements BugService {
    private final BugRepository bugRepository;
    private final BugMapper bugMapper;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Override
    public BugResponseDTO createBug(BugRequestDTO requestDTO) {
        if (requestDTO.getProjectId() == null) {
            throw new IllegalArgumentException("projectId không được null");
        }
        if (requestDTO.getReporterId() == null) {
            throw new IllegalArgumentException("reporterId không được null");
        }

        Project project = projectRepository.findById(requestDTO.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Project với id: " + requestDTO.getProjectId()));

        User reporter = userRepository.findById(requestDTO.getReporterId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy User với id: " + requestDTO.getReporterId()));

        Tasks task = null;
        if (requestDTO.getOriginalTaskId() != null) {
            task = taskRepository.findById(requestDTO.getOriginalTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Task với id: " + requestDTO.getOriginalTaskId()));
        }

        Bug bug = new Bug();
        bug.setTitle(requestDTO.getTitle());
        bug.setDescription(requestDTO.getDescription());
        bug.setStatus(requestDTO.getStatus());
        bug.setSeverity(requestDTO.getSeverity());
        bug.setProject(project);
        bug.setReporter(reporter);
        bug.setOriginalTask(task);

        Bug savedBug = bugRepository.save(bug);
        return bugMapper.toResponseDto(savedBug);
    }

    @Override
    public BugResponseDTO updateBug(UUID id, BugRequestDTO requestDTO) {
        Bug bug = bugRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bug với id: " + id));

        // Chỉ update các trường cơ bản, không thay đổi Project/Reporter/Task
        bug.setTitle(requestDTO.getTitle());
        bug.setDescription(requestDTO.getDescription());
        bug.setStatus(requestDTO.getStatus());
        bug.setSeverity(requestDTO.getSeverity());

        bugRepository.save(bug);
        return bugMapper.toResponseDto(bug);
    }

    @Override
    public void deleteBug(UUID id) {
        Bug bug = bugRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bug với id: " + id));
        bugRepository.delete(bug);
    }

    @Override
    public BugResponseDTO getBugById(UUID id) {
        Bug bug = bugRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bug với id: " + id));
        return bugMapper.toResponseDto(bug);
    }

    @Override
    public List<BugResponseDTO> getBugsByProjectId(Integer projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Project với id: " + projectId));

        List<Bug> bugs = bugRepository.findByProjectId(projectId);
        return bugs.stream()
                .map(bugMapper::toResponseDto)
                .toList();
    }
}
