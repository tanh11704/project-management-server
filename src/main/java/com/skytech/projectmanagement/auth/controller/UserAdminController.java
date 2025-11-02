package com.skytech.projectmanagement.auth.controller;

import java.util.List;
import com.skytech.projectmanagement.auth.dto.PermissionResponse;
import com.skytech.projectmanagement.auth.dto.RoleResponse;
import com.skytech.projectmanagement.auth.dto.SyncUserPermissionsRequest;
import com.skytech.projectmanagement.auth.dto.UpdateUserRolesRequest;
import com.skytech.projectmanagement.auth.service.RoleService;
import com.skytech.projectmanagement.common.dto.SuccessResponse;
import com.skytech.projectmanagement.user.dto.ResetPasswordsRequest;
import com.skytech.projectmanagement.user.dto.ResetPasswordsResponse;
import com.skytech.projectmanagement.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/auth-service/v1/admin/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final RoleService roleService;
    private final UserService userService;

    @PutMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<SuccessResponse<List<RoleResponse>>> syncRolesForUser(
            @PathVariable Integer userId, @Valid @RequestBody UpdateUserRolesRequest request) {

        List<RoleResponse> updatedRoles = roleService.syncRolesForUser(userId, request);

        SuccessResponse<List<RoleResponse>> response =
                SuccessResponse.of(updatedRoles, "Cập nhật vai trò cho user thành công.");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-passwords")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<SuccessResponse<ResetPasswordsResponse>> resetPasswords(
            @Valid @RequestBody ResetPasswordsRequest request) {

        ResetPasswordsResponse response = userService.resetPasswordsForUsers(request);

        String message = String.format(
                "Đã reset mật khẩu cho %d người dùng. %d người dùng không thành công.",
                response.getTotalReset(), response.getTotalFailed());

        return ResponseEntity.ok(SuccessResponse.of(response, message));
    }

    @PutMapping("/{userId}/permissions")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<SuccessResponse<List<PermissionResponse>>> syncPermissionsForUser(
            @PathVariable Integer userId, @Valid @RequestBody SyncUserPermissionsRequest request) {

        List<PermissionResponse> updatedPermissions =
                roleService.syncPermissionsForUser(userId, request);

        SuccessResponse<List<PermissionResponse>> response = SuccessResponse.of(updatedPermissions,
                "Cập nhật quyền cho người dùng thành công. Chỉ các quyền con (leaf permissions) được lưu trữ.");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/permissions")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<SuccessResponse<List<PermissionResponse>>> getPermissionsForUser(
            @PathVariable Integer userId) {

        List<PermissionResponse> permissions = roleService.getPermissionsByUserId(userId);

        SuccessResponse<List<PermissionResponse>> response =
                SuccessResponse.of(permissions, "Lấy quyền của người dùng thành công.");

        return ResponseEntity.ok(response);
    }
}
