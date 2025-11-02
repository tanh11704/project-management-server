package com.skytech.projectmanagement.Bug.service;

import com.skytech.projectmanagement.Bug.dto.BugAssigneesDTO;

import java.util.List;
import java.util.UUID;

public interface BugAssigneesService {
    BugAssigneesDTO assignUserToBug(UUID bugId, Integer userId);
    void unassignUserFromBug(UUID bugId, Integer userId);
    List<BugAssigneesDTO> getAssigneesByBug(UUID bugId);
}
