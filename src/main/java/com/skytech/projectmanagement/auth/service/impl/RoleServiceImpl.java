package com.skytech.projectmanagement.auth.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.skytech.projectmanagement.auth.dto.CreateRoleRequest;
import com.skytech.projectmanagement.auth.dto.PermissionResponse;
import com.skytech.projectmanagement.auth.dto.RoleResponse;
import com.skytech.projectmanagement.auth.dto.SyncRolePermissionsRequest;
import com.skytech.projectmanagement.auth.dto.SyncUserPermissionsRequest;
import com.skytech.projectmanagement.auth.dto.UpdateUserRolesRequest;
import com.skytech.projectmanagement.auth.entity.Permission;
import com.skytech.projectmanagement.auth.entity.Role;
import com.skytech.projectmanagement.auth.entity.RolePermission;
import com.skytech.projectmanagement.auth.entity.RolePermissionId;
import com.skytech.projectmanagement.auth.entity.UserPermission;
import com.skytech.projectmanagement.auth.entity.UserPermissionId;
import com.skytech.projectmanagement.auth.entity.UserRole;
import com.skytech.projectmanagement.auth.entity.UserRoleId;
import com.skytech.projectmanagement.auth.repository.PermissionRepository;
import com.skytech.projectmanagement.auth.repository.RolePermissionRepository;
import com.skytech.projectmanagement.auth.repository.RoleRepository;
import com.skytech.projectmanagement.auth.repository.UserPermissionRepository;
import com.skytech.projectmanagement.auth.repository.UserRoleRepository;
import com.skytech.projectmanagement.auth.service.PermissionService;
import com.skytech.projectmanagement.auth.service.RoleService;
import com.skytech.projectmanagement.common.exception.ResourceNotFoundException;
import com.skytech.projectmanagement.common.exception.RoleNameExistsException;
import com.skytech.projectmanagement.common.exception.ValidationException;
import com.skytech.projectmanagement.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final UserService userService;
    private final PermissionService permissionService;

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {

        return roleRepository.findAll().stream().map(RoleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream().map(PermissionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getPermissionsByRoleId(Integer roleId) {

        if (!roleRepository.existsById(roleId)) {
            throw new ResourceNotFoundException("Không tìm thấy vai trò với ID: " + roleId);
        }

        return permissionRepository.findAllByRoleId(roleId).stream()
                .map(PermissionResponse::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<PermissionResponse> syncPermissionsForRole(Integer roleId,
            SyncRolePermissionsRequest request) {

        if (!roleRepository.existsById(roleId)) {
            throw new ResourceNotFoundException("Không tìm thấy vai trò với ID: " + roleId);
        }

        List<Integer> requestedIds = request.permissionIds();

        long validIdsCount = permissionRepository.countByIdIn(requestedIds);
        if (validIdsCount != requestedIds.size()) {
            throw new ValidationException(
                    "Dữ liệu không hợp lệ: Một hoặc nhiều Permission ID không tồn tại.");
        }

        rolePermissionRepository.deleteById_RoleId(roleId);

        List<RolePermission> newPermissions = requestedIds.stream().map(permissionId -> {
            RolePermissionId id = new RolePermissionId();
            id.setRoleId(roleId);
            id.setPermissionId(permissionId);

            RolePermission rp = new RolePermission();
            rp.setId(id);
            return rp;
        }).collect(Collectors.toList());

        rolePermissionRepository.saveAll(newPermissions);

        List<Permission> updatedPermissions = permissionRepository.findByIdIn(requestedIds);

        return updatedPermissions.stream().map(PermissionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<RoleResponse> syncRolesForUser(Integer userId, UpdateUserRolesRequest request) {

        if (!userService.existsById(userId)) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId);
        }

        List<Integer> requestedRoleIds = request.roleIds();

        long validIdsCount = roleRepository.countByIdIn(requestedRoleIds);
        if (validIdsCount != requestedRoleIds.size()) {
            throw new ValidationException(
                    "Dữ liệu không hợp lệ: Một hoặc nhiều Role ID không tồn tại.");
        }

        userRoleRepository.deleteById_UserId(userId);

        List<UserRole> newRoles = requestedRoleIds.stream().map(roleId -> {
            UserRoleId id = new UserRoleId();
            id.setUserId(userId);
            id.setRoleId(roleId);

            UserRole ur = new UserRole();
            ur.setId(id);
            return ur;
        }).collect(Collectors.toList());

        userRoleRepository.saveAll(newRoles);

        List<Role> updatedRoles = roleRepository.findByIdIn(requestedRoleIds);

        return updatedRoles.stream().map(RoleResponse::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {

        if (roleRepository.existsByNameIgnoreCase(request.name())) {
            throw new RoleNameExistsException(
                    "Tên vai trò '" + request.name() + "' đã được sử dụng.");
        }

        Role newRole = new Role();
        newRole.setName(request.name().toUpperCase());
        newRole.setDescription(request.description());

        Role savedRole = roleRepository.save(newRole);

        return RoleResponse.fromEntity(savedRole);
    }

    @Override
    @Transactional
    public List<PermissionResponse> syncPermissionsForUser(Integer userId,
            SyncUserPermissionsRequest request) {

        if (!userService.existsById(userId)) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId);
        }

        List<Integer> requestedIds = request.getPermissionIds();

        long validIdsCount = permissionRepository.countByIdIn(requestedIds);
        if (validIdsCount != requestedIds.size()) {
            throw new ValidationException(
                    "Dữ liệu không hợp lệ: Một hoặc nhiều Permission ID không tồn tại.");
        }

        Set<Integer> leafPermissionIds =
                permissionService.filterLeafPermissionIds(new java.util.HashSet<>(requestedIds));

        userPermissionRepository.deleteById_UserId(userId);

        List<UserPermission> newPermissions = leafPermissionIds.stream().map(permissionId -> {
            UserPermissionId id = new UserPermissionId();
            id.setUserId(userId);
            id.setPermissionId(permissionId);

            UserPermission up = new UserPermission();
            up.setId(id);
            return up;
        }).collect(Collectors.toList());

        userPermissionRepository.saveAll(newPermissions);

        List<Permission> updatedPermissions =
                permissionRepository.findByIdIn(new java.util.ArrayList<>(leafPermissionIds));

        return updatedPermissions.stream().map(PermissionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getPermissionsByUserId(Integer userId) {
        if (!userService.existsById(userId)) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId);
        }

        List<Permission> allPermissions = permissionRepository.findAll();

        Set<Integer> userPermissionIds = userPermissionRepository.findAll().stream()
                .filter(up -> up.getId().getUserId().equals(userId))
                .map(up -> up.getId().getPermissionId()).collect(Collectors.toSet());

        List<Permission> userPermissions = allPermissions.stream()
                .filter(p -> userPermissionIds.contains(p.getId())).collect(Collectors.toList());

        return userPermissions.stream().map(PermissionResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
