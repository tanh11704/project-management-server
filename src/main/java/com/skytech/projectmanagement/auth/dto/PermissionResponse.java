package com.skytech.projectmanagement.auth.dto;

import com.skytech.projectmanagement.auth.entity.Permission;

public record PermissionResponse(Integer id, String name, String description, Integer parentId) {

    public static PermissionResponse fromEntity(Permission permission) {
        Integer parentId = permission.getParent() != null ? permission.getParent().getId() : null;
        return new PermissionResponse(permission.getId(), permission.getName(),
                permission.getDescription(), parentId);
    }
}
