package com.skytech.projectmanagement.project.controller;

import java.util.List;
import com.skytech.projectmanagement.common.dto.PaginatedResponse;
import com.skytech.projectmanagement.common.dto.SuccessResponse;
import com.skytech.projectmanagement.project.dto.AddMemberRequest;
import com.skytech.projectmanagement.project.dto.CreateProjectRequest;
import com.skytech.projectmanagement.project.dto.ImportTeamRequest;
import com.skytech.projectmanagement.project.dto.ProjectDetailsResponse;
import com.skytech.projectmanagement.project.dto.ProjectMemberResponse;
import com.skytech.projectmanagement.project.dto.ProjectSummaryResponse;
import com.skytech.projectmanagement.project.dto.UpdateMemberRoleRequest;
import com.skytech.projectmanagement.project.dto.UpdateProjectRequest;
import com.skytech.projectmanagement.project.service.ProjectService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/project-service/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @DeleteMapping("/{projectId}/members/{userId}")
    @PreAuthorize("hasAuthority('PROJECT_MEMBER_MANAGE')")
    public ResponseEntity<SuccessResponse<Object>> removeProjectMember(
            @PathVariable Integer projectId, @PathVariable Integer userId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        projectService.removeProjectMember(projectId, userId, authentication);

        SuccessResponse<Object> response = SuccessResponse.of(null, "Xóa thành viên thành công.");

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{projectId}/members/{userId}")
    @PreAuthorize("hasAuthority('PROJECT_MEMBER_MANAGE')")
    public ResponseEntity<SuccessResponse<ProjectMemberResponse>> updateMemberRole(
            @PathVariable Integer projectId, @PathVariable Integer userId,
            @Valid @RequestBody UpdateMemberRoleRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        ProjectMemberResponse updatedMember =
                projectService.updateMemberRole(projectId, userId, request, authentication);

        SuccessResponse<ProjectMemberResponse> response =
                SuccessResponse.of(updatedMember, "Cập nhật vai trò thành công.");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{projectId}/members/import")
    @PreAuthorize("hasAuthority('PROJECT_MEMBER_MANAGE')")
    public ResponseEntity<SuccessResponse<List<ProjectMemberResponse>>> importTeamMembers(
            @PathVariable Integer projectId, @Valid @RequestBody ImportTeamRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        List<ProjectMemberResponse> importedMembers =
                projectService.importTeamMembers(projectId, request, authentication);

        String message = importedMembers.isEmpty()
                ? "Không có thành viên mới được thêm vào. Tất cả thành viên của team đã có trong dự án."
                : "Đã thêm " + importedMembers.size() + " thành viên từ team vào dự án.";

        SuccessResponse<List<ProjectMemberResponse>> response =
                SuccessResponse.of(importedMembers, message);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{projectId}/members")
    @PreAuthorize("hasAuthority('PROJECT_MEMBER_MANAGE')")
    public ResponseEntity<SuccessResponse<List<ProjectMemberResponse>>> addProjectMembers(
            @PathVariable Integer projectId, @Valid @RequestBody List<AddMemberRequest> requests) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        List<ProjectMemberResponse> newMembers =
                projectService.addProjectMembers(projectId, requests, authentication);

        SuccessResponse<List<ProjectMemberResponse>> response = SuccessResponse.of(newMembers,
                "Thêm " + newMembers.size() + " thành viên thành công.");

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{projectId}/members")
    @PreAuthorize("hasAuthority('PROJECT_MEMBER_MANAGE')")
    public ResponseEntity<SuccessResponse<List<ProjectMemberResponse>>> getProjectMembers(
            @PathVariable Integer projectId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        List<ProjectMemberResponse> members =
                projectService.getProjectMembers(projectId, authentication);

        SuccessResponse<List<ProjectMemberResponse>> response =
                SuccessResponse.of(members, "Lấy danh sách thành viên thành công.");

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{projectId}")
    @PreAuthorize("hasAuthority('PROJECT_UPDATE')")
    public ResponseEntity<SuccessResponse<ProjectSummaryResponse>> updateProject(
            @PathVariable Integer projectId, @Valid @RequestBody UpdateProjectRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        ProjectSummaryResponse updatedProject =
                projectService.updateProject(projectId, request, authentication);

        SuccessResponse<ProjectSummaryResponse> response =
                SuccessResponse.of(updatedProject, "Cập nhật dự án thành công.");

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PROJECT_CREATE')")
    public ResponseEntity<SuccessResponse<ProjectSummaryResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        ProjectSummaryResponse newProjectDto =
                projectService.createProject(request, authentication);

        SuccessResponse<ProjectSummaryResponse> response =
                SuccessResponse.of(newProjectDto, "Tạo dự án thành công.");

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasAuthority('PROJECT_DELETE')")
    public ResponseEntity<SuccessResponse<Object>> deleteProject(@PathVariable Integer projectId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        projectService.deleteProject(projectId, authentication);

        SuccessResponse<Object> response = SuccessResponse.of(null, "Xóa dự án thành công.");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<SuccessResponse<ProjectDetailsResponse>> getProjectById(
            @PathVariable Integer projectId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        ProjectDetailsResponse projectData =
                projectService.getProjectDetails(projectId, authentication);

        SuccessResponse<ProjectDetailsResponse> response =
                SuccessResponse.of(projectData, "Lấy thông tin dự án thành công.");

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<ProjectSummaryResponse>> getProjects(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {

        Sort sort = Sort.by(
                sortOrder.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        PaginatedResponse<ProjectSummaryResponse> response =
                projectService.getProjects(pageable, search, authentication);

        return ResponseEntity.ok(response);
    }

}
