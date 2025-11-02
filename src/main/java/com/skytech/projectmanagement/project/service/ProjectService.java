package com.skytech.projectmanagement.project.service;

import java.util.List;
import com.skytech.projectmanagement.common.dto.PaginatedResponse;
import com.skytech.projectmanagement.project.dto.AddMemberRequest;
import com.skytech.projectmanagement.project.dto.CreateProjectRequest;
import com.skytech.projectmanagement.project.dto.ImportTeamRequest;
import com.skytech.projectmanagement.project.dto.ProjectDetailsResponse;
import com.skytech.projectmanagement.project.dto.ProjectMemberResponse;
import com.skytech.projectmanagement.project.dto.ProjectSummaryResponse;
import com.skytech.projectmanagement.project.dto.UpdateMemberRoleRequest;
import com.skytech.projectmanagement.project.dto.UpdateProjectRequest;
import com.skytech.projectmanagement.project.entity.Project;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface ProjectService {

    List<ProjectMemberResponse> importTeamMembers(Integer projectId, ImportTeamRequest request,
            Authentication auth);

    void removeProjectMember(Integer projectId, Integer userId, Authentication auth);

    ProjectMemberResponse updateMemberRole(Integer projectId, Integer userId,
            UpdateMemberRoleRequest request, Authentication auth);

    List<ProjectMemberResponse> addProjectMembers(Integer projectId,
            List<AddMemberRequest> requests, Authentication auth);

    ProjectSummaryResponse updateProject(Integer projectId, UpdateProjectRequest request,
            Authentication authentication);

    ProjectSummaryResponse createProject(CreateProjectRequest request,
            Authentication authentication);

    void deleteProject(Integer projectId, Authentication authentication);

    ProjectDetailsResponse getProjectDetails(Integer projectId, Authentication authentication);

    PaginatedResponse<ProjectSummaryResponse> getProjects(Pageable pageable, String search,
            Authentication authentication);

    List<ProjectMemberResponse> getProjectMembers(Integer projectId, Authentication authentication);
    Project getProjectEntityById(Integer projectId);
}
