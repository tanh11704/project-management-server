package com.skytech.projectmanagement.auth.controller;

import java.util.List;
import com.skytech.projectmanagement.auth.dto.CreateRoleRequest;
import com.skytech.projectmanagement.auth.dto.PermissionResponse;
import com.skytech.projectmanagement.auth.dto.RoleResponse;
import com.skytech.projectmanagement.auth.dto.SyncRolePermissionsRequest;
import com.skytech.projectmanagement.auth.dto.UpdateRoleRequest;
import com.skytech.projectmanagement.auth.service.RoleService;
import com.skytech.projectmanagement.common.dto.SuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth-service/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<SuccessResponse<List<RoleResponse>>> getAllRoles() {

        List<RoleResponse> roles = roleService.getAllRoles();

        SuccessResponse<List<RoleResponse>> response =
                SuccessResponse.of(roles, "Lấy danh sách vai trò thành công.");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<SuccessResponse<List<PermissionResponse>>> getPermissionsForRole(
            @PathVariable Integer roleId) {

        List<PermissionResponse> permissions = roleService.getPermissionsByRoleId(roleId);

        SuccessResponse<List<PermissionResponse>> response =
                SuccessResponse.of(permissions, "Lấy quyền của vai trò thành công.");

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<SuccessResponse<List<PermissionResponse>>> syncPermissionsForRole(
            @PathVariable Integer roleId, @Valid @RequestBody SyncRolePermissionsRequest request) {

        List<PermissionResponse> updatedPermissions =
                roleService.syncPermissionsForRole(roleId, request);

        SuccessResponse<List<PermissionResponse>> response =
                SuccessResponse.of(updatedPermissions, "Cập nhật quyền cho vai trò thành công.");

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<SuccessResponse<RoleResponse>> createRole(
            @Valid @RequestBody CreateRoleRequest request) {

        RoleResponse newRole = roleService.createRole(request);

        SuccessResponse<RoleResponse> response =
                SuccessResponse.of(newRole, "Tạo vai trò thành công.");

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<SuccessResponse<RoleResponse>> updateRole(@PathVariable Integer roleId,
            @Valid @RequestBody UpdateRoleRequest request) {

        RoleResponse updatedRole = roleService.updateRole(roleId, request);

        SuccessResponse<RoleResponse> response =
                SuccessResponse.of(updatedRole, "Cập nhật vai trò thành công.");

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<SuccessResponse<Void>> deleteRole(@PathVariable Integer roleId) {

        roleService.deleteRole(roleId);

        SuccessResponse<Void> response = SuccessResponse.of(null, "Xóa vai trò thành công.");

        return ResponseEntity.ok(response);
    }
}
