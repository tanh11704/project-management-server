package com.skytech.projectmanagement.auth.dto;

import java.util.List;

public record UserLoginResponse(Integer id, String fullName, String email, String avatar,
        List<PermissionTreeNode> permissionTree) {
}
