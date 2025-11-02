package com.skytech.projectmanagement.Bug.service;

import com.skytech.projectmanagement.Bug.dto.BugRequestDTO;
import com.skytech.projectmanagement.Bug.dto.BugResponseDTO;
import com.skytech.projectmanagement.Bug.entity.Bug;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BugService {
    BugResponseDTO createBug(BugRequestDTO requestDTO);
    BugResponseDTO updateBug(UUID id, BugRequestDTO requestDTO);
    void deleteBug(UUID id);
    BugResponseDTO getBugById(UUID id);
    List<BugResponseDTO> getBugsByProjectId(Integer projectId);
}
