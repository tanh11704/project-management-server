package com.skytech.projectmanagement.project.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import com.skytech.projectmanagement.common.dto.PaginatedResponse;
import com.skytech.projectmanagement.common.dto.Pagination;
import com.skytech.projectmanagement.common.exception.DeleteConflictException;
import com.skytech.projectmanagement.common.exception.MemberAlreadyExistsException;
import com.skytech.projectmanagement.common.exception.MemberNotFoundException;
import com.skytech.projectmanagement.common.exception.ProjectKeyExistsException;
import com.skytech.projectmanagement.common.exception.ResourceNotFoundException;
import com.skytech.projectmanagement.project.dto.AddMemberRequest;
import com.skytech.projectmanagement.project.dto.CreateProjectRequest;
import com.skytech.projectmanagement.project.dto.ImportTeamRequest;
import com.skytech.projectmanagement.project.dto.ProjectDetailsResponse;
import com.skytech.projectmanagement.project.dto.ProjectMemberResponse;
import com.skytech.projectmanagement.project.dto.ProjectSummaryResponse;
import com.skytech.projectmanagement.project.dto.UpdateMemberRoleRequest;
import com.skytech.projectmanagement.project.dto.UpdateProjectRequest;
import com.skytech.projectmanagement.project.entity.Project;
import com.skytech.projectmanagement.project.entity.ProjectMember;
import com.skytech.projectmanagement.project.entity.ProjectMemberId;
import com.skytech.projectmanagement.project.entity.ProjectRole;
import com.skytech.projectmanagement.project.repository.ProjectMemberRepository;
import com.skytech.projectmanagement.project.repository.ProjectRepository;
import com.skytech.projectmanagement.project.service.ProjectService;
import com.skytech.projectmanagement.teams.entity.TeamMember;
import com.skytech.projectmanagement.teams.repository.TeamMemberRepository;
import com.skytech.projectmanagement.user.entity.User;
import com.skytech.projectmanagement.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserService userService;
    private final TeamMemberRepository teamMemberRepository;

    @Override
    public PaginatedResponse<ProjectSummaryResponse> getProjects(Pageable pageable, String search,
            Authentication authentication) {

        Specification<Project> searchSpec = createSearchSpecification(search);

        Specification<Project> authSpec = createAuthorizationSpecification(authentication);

        Specification<Project> finalSpec = searchSpec.and(authSpec);

        Page<Project> projectPage = projectRepository.findAll(finalSpec, pageable);

        List<ProjectSummaryResponse> dtoList =
                projectPage.stream().map(ProjectSummaryResponse::fromEntity).toList();
        Pagination pagination = new Pagination(projectPage.getNumber(), projectPage.getSize(),
                projectPage.getTotalElements(), projectPage.getTotalPages());
        return PaginatedResponse.of(dtoList, pagination, "Lấy danh sách dự án thành công.");

    }

    @Override
    public ProjectDetailsResponse getProjectDetails(Integer projectId,
            Authentication authentication) {

        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy dự án với ID: " + projectId));

        checkProjectAccess(projectId, authentication);

        List<ProjectMember> members = projectMemberRepository.findById_ProjectId(projectId);

        List<ProjectMemberResponse> memberResponses = new ArrayList<>();

        if (!members.isEmpty()) {
            Set<Integer> userIds = members.stream().map(member -> member.getId().getUserId())
                    .collect(Collectors.toSet());

            Map<Integer, User> userMap = userService.findUsersMapByIds(userIds);

            memberResponses = members.stream().map(member -> {
                User user = userMap.get(member.getId().getUserId());
                String name = (user != null) ? user.getFullName() : "Người dùng không tồn tại";
                String avatar = (user != null) ? user.getAvatar() : null;

                return new ProjectMemberResponse(member.getId().getUserId(), name, avatar,
                        member.getRole(), member.getJoinedAt());
            }).toList();
        }

        return ProjectDetailsResponse.fromEntity(project, memberResponses);
    }

    @Override
    public void deleteProject(Integer projectId, Authentication authentication) {
        String email = authentication.getName();
        User currentUser = userService.findUserByEmail(email);

        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy dự án với ID: " + projectId));

        checkAdminOrCreatorPermission(project, currentUser, authentication);

        projectMemberRepository.deleteById_ProjectId(projectId);

        projectRepository.delete(project);
    }

    @Override
    @Transactional
    public ProjectSummaryResponse createProject(CreateProjectRequest request,
            Authentication authentication) {

        if (projectRepository.existsByProjectKeyIgnoreCase(request.projectKey())) {
            throw new ProjectKeyExistsException(
                    "Project key '" + request.projectKey() + "' đã được sử dụng.");
        }

        String email = authentication.getName();
        User currentUser = userService.findUserByEmail(email);

        Project newProject = new Project();
        newProject.setProjectName(request.projectName());
        newProject.setProjectKey(request.projectKey().toUpperCase());
        newProject.setDescription(request.description());
        newProject.setDueDate(request.dueDate());
        newProject.setCreatedBy(currentUser.getId());

        Project savedProject = projectRepository.save(newProject);

        // Tự động thêm người tạo làm thành viên (ví dụ: PM)
        ProjectMember creatorAsMember = new ProjectMember();

        ProjectMemberId projectMemberId = new ProjectMemberId();
        projectMemberId.setProjectId(savedProject.getId());
        projectMemberId.setUserId(currentUser.getId());

        creatorAsMember.setId(projectMemberId);
        creatorAsMember.setRole(ProjectRole.PM);
        projectMemberRepository.save(creatorAsMember);

        return ProjectSummaryResponse.fromEntity(savedProject);
    }

    @Override
    public ProjectSummaryResponse updateProject(Integer projectId, UpdateProjectRequest request,
            Authentication authentication) {

        User currentUser = userService.findUserByEmail(authentication.getName());

        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy dự án với ID: " + projectId));

        checkAdminOrCreatorPermission(project, currentUser, authentication);

        if (request.projectName() != null) {
            project.setProjectName(request.projectName());
        }
        if (request.description() != null) {
            project.setDescription(request.description());
        }
        if (request.dueDate() != null) {
            project.setDueDate(request.dueDate());
        }

        Project updatedProject = projectRepository.save(project);
        return ProjectSummaryResponse.fromEntity(updatedProject);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getProjectMembers(Integer projectId,
            Authentication authentication) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Không tìm thấy dự án với ID: " + projectId);
        }

        checkProjectAccess(projectId, authentication);

        List<ProjectMember> members = projectMemberRepository.findById_ProjectId(projectId);

        if (members.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Integer> userIds = members.stream().map(member -> member.getId().getUserId())
                .collect(Collectors.toSet());

        Map<Integer, User> userMap = userService.findUsersMapByIds(userIds);

        return members.stream().map(member -> {
            User user = userMap.get(member.getId().getUserId());
            String name = (user != null) ? user.getFullName() : "Người dùng không tồn tại";
            String avatar = (user != null) ? user.getAvatar() : null;

            return new ProjectMemberResponse(member.getId().getUserId(), name, avatar,
                    member.getRole(), member.getJoinedAt());
        }).toList();
    }

    @Override
    public List<ProjectMemberResponse> addProjectMembers(Integer projectId,
            List<AddMemberRequest> requests, Authentication auth) {

        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy dự án với ID: " + projectId));

        User currentUser = userService.findUserByEmail(auth.getName());
        checkAdminOrCreatorPermission(project, currentUser, auth);

        Set<Integer> userIds =
                requests.stream().map(AddMemberRequest::userId).collect(Collectors.toSet());

        userService.validateUsersExist(userIds);

        List<ProjectMember> newMembers = new ArrayList<>();
        for (AddMemberRequest req : requests) {
            if (projectMemberRepository.existsById_UserIdAndId_ProjectId(req.userId(), projectId)) {
                throw new MemberAlreadyExistsException(
                        "Người dùng với ID '" + req.userId() + "' đã là thành viên của dự án này.");
            }
            ProjectMember newMember = new ProjectMember();

            ProjectMemberId projectMemberId = new ProjectMemberId();
            projectMemberId.setUserId(req.userId());
            projectMemberId.setProjectId(projectId);

            newMember.setId(projectMemberId);
            newMember.setRole(req.role());
            newMembers.add(newMember);
        }

        List<ProjectMember> savedMembers = projectMemberRepository.saveAll(newMembers);

        Map<Integer, User> userMap = userService.findUsersMapByIds(userIds);

        return savedMembers.stream().map(member -> {
            User user = userMap.get(member.getId().getUserId());
            return new ProjectMemberResponse(member.getId().getUserId(),
                    (user != null) ? user.getFullName() : "N/A",
                    (user != null) ? user.getAvatar() : null, member.getRole(),
                    member.getJoinedAt());
        }).toList();
    }

    @Override
    @Transactional
    public ProjectMemberResponse updateMemberRole(Integer projectId, Integer userId,
            UpdateMemberRoleRequest request, Authentication auth) {

        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy dự án với ID: " + projectId));
        User currentUser = userService.findUserByEmail(auth.getName());
        checkAdminOrCreatorPermission(project, currentUser, auth);

        ProjectMemberId memberId = new ProjectMemberId();
        memberId.setUserId(userId);
        memberId.setProjectId(projectId);

        ProjectMember member = projectMemberRepository.findById(memberId).orElseThrow(
                () -> new MemberNotFoundException("Không tìm thấy thành viên với user ID '" + userId
                        + "' trong dự án '" + projectId + "'."));

        member.setRole(request.role());
        ProjectMember updatedMember = projectMemberRepository.save(member);

        Map<Integer, User> userMap = userService.findUsersMapByIds(Set.of(userId));
        User user = userMap.get(userId);

        return new ProjectMemberResponse(updatedMember.getId().getUserId(),
                (user != null) ? user.getFullName() : "N/A",
                (user != null) ? user.getAvatar() : null, updatedMember.getRole(),
                updatedMember.getJoinedAt());
    }

    @Override
    @Transactional
    public void removeProjectMember(Integer projectId, Integer userId, Authentication auth) {

        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy dự án với ID: " + projectId));

        User currentUser = userService.findUserByEmail(auth.getName());

        checkAdminOrCreatorPermission(project, currentUser, auth);

        ProjectMemberId memberId = new ProjectMemberId();
        memberId.setUserId(userId);
        memberId.setProjectId(projectId);

        if (!projectMemberRepository.existsById(memberId)) {
            throw new MemberNotFoundException("Không tìm thấy thành viên với user ID '" + userId
                    + "' trong dự án '" + projectId + "'.");
        }

        if (Objects.equals(project.getCreatedBy(), userId)) {
            throw new DeleteConflictException(
                    "Không thể xóa người tạo dự án (Project Creator) khỏi dự án.");
        }

        projectMemberRepository.deleteById(memberId);
    }

    private void checkAdminOrCreatorPermission(Project project, User currentUser,
            Authentication authentication) {
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());

        if (roles.contains("ROLE_PRODUCT_OWNER")) {
            return;
        }

        if (Objects.equals(project.getCreatedBy(), currentUser.getId())) {
            return;
        }

        throw new AccessDeniedException("Bạn không có quyền thực hiện hành động này.");
    }

    private void checkProjectAccess(Integer projectId, Authentication authentication) {
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());

        if (roles.contains("ROLE_PRODUCT_OWNER")) {
            return;
        }

        String email = authentication.getName();
        User currentUser = userService.findUserByEmail(email);

        boolean isMember = projectMemberRepository
                .existsById_UserIdAndId_ProjectId(currentUser.getId(), projectId);

        if (!isMember) {
            throw new AccessDeniedException("Bạn không có quyền xem dự án này.");
        }
    }

    private Specification<Project> createSearchSpecification(String search) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(search)) {
                return cb.conjunction();
            }
            String lowerCaseSearch = "%" + search.toLowerCase() + "%";
            Predicate nameLike = cb.like(cb.lower(root.get("projectName")), lowerCaseSearch);
            Predicate keyLike = cb.like(cb.lower(root.get("projectKey")), lowerCaseSearch);
            return cb.or(nameLike, keyLike);
        };
    }

    private Specification<Project> createAuthorizationSpecification(Authentication authentication) {
        Set<String> permissions = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());

        if (permissions.contains("PROJECT_READ_ALL")) {
            return (root, query, cb) -> cb.conjunction();
        }

        return (root, query, cb) -> {
            String email = authentication.getName();
            User currentUser = userService.findUserByEmail(email);

            List<ProjectMember> memberships =
                    projectMemberRepository.findById_UserId(currentUser.getId());

            if (memberships.isEmpty()) {
                return cb.disjunction();
            }

            List<Integer> projectIds =
                    memberships.stream().map(pm -> pm.getId().getProjectId()).toList();

            return root.get("id").in(projectIds);
        };
    }

    @Override
    @Transactional
    public List<ProjectMemberResponse> importTeamMembers(Integer projectId,
            ImportTeamRequest request, Authentication auth) {

        // 1. Kiểm tra project tồn tại
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy dự án với ID: " + projectId));

        // 2. Kiểm tra quyền (admin hoặc creator)
        User currentUser = userService.findUserByEmail(auth.getName());
        checkAdminOrCreatorPermission(project, currentUser, auth);

        // 3. Lấy danh sách user IDs từ team
        List<TeamMember> teamMembers = teamMemberRepository.findByTeamId(request.teamId());

        if (teamMembers.isEmpty()) {
            return List.of();
        }

        Set<Integer> userIdsFromTeam =
                teamMembers.stream().map(TeamMember::getUserId).collect(Collectors.toSet());

        // 4. Lấy danh sách thành viên đã có trong dự án
        List<ProjectMember> existingMembers = projectMemberRepository.findById_ProjectId(projectId);

        Set<Integer> existingUserIds = existingMembers.stream()
                .map(member -> member.getId().getUserId()).collect(Collectors.toSet());

        // 5. Lọc ra những user ID MỚI (chưa có trong project)
        Set<Integer> newUserIds = userIdsFromTeam.stream()
                .filter(userId -> !existingUserIds.contains(userId)).collect(Collectors.toSet());

        // Nếu không có ai mới -> trả về rỗng
        if (newUserIds.isEmpty()) {
            return List.of();
        }

        // 6. Kiểm tra user tồn tại
        userService.validateUsersExist(newUserIds);

        // 7. Tạo và lưu các thành viên mới với defaultRole
        List<ProjectMember> newMembers = new ArrayList<>();
        for (Integer userId : newUserIds) {
            ProjectMember newMember = new ProjectMember();

            ProjectMemberId projectMemberId = new ProjectMemberId();
            projectMemberId.setUserId(userId);
            projectMemberId.setProjectId(projectId);

            newMember.setId(projectMemberId);
            newMember.setRole(request.defaultRole());
            newMembers.add(newMember);
        }

        List<ProjectMember> savedMembers = projectMemberRepository.saveAll(newMembers);

        // 8. Lấy thông tin đầy đủ của users
        Map<Integer, User> userMap = userService.findUsersMapByIds(newUserIds);

        return savedMembers.stream().map(member -> {
            User user = userMap.get(member.getId().getUserId());
            return new ProjectMemberResponse(member.getId().getUserId(),
                    (user != null) ? user.getFullName() : "N/A",
                    (user != null) ? user.getAvatar() : null, member.getRole(),
                    member.getJoinedAt());
        }).toList();
    }

}
