package com.skytech.projectmanagement.auth.service;

import java.util.List;
import com.skytech.projectmanagement.auth.dto.CreateRoleRequest;
import com.skytech.projectmanagement.auth.dto.PermissionResponse;
import com.skytech.projectmanagement.auth.dto.RoleResponse;
import com.skytech.projectmanagement.auth.dto.SyncRolePermissionsRequest;
import com.skytech.projectmanagement.auth.dto.SyncUserPermissionsRequest;
import com.skytech.projectmanagement.auth.dto.UpdateUserRolesRequest;

public interface RoleService {

    List<RoleResponse> getAllRoles();

    List<PermissionResponse> getAllPermissions();

    List<PermissionResponse> getPermissionsByRoleId(Integer roleId);

    List<PermissionResponse> syncPermissionsForRole(Integer roleId,
            SyncRolePermissionsRequest request);

    List<RoleResponse> syncRolesForUser(Integer userId, UpdateUserRolesRequest request);

    RoleResponse createRole(CreateRoleRequest request);

    List<PermissionResponse> syncPermissionsForUser(Integer userId,
            SyncUserPermissionsRequest request);

    List<PermissionResponse> getPermissionsByUserId(Integer userId);
}
