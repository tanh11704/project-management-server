package com.skytech.projectmanagement.auth.dto;

import java.util.ArrayList;
import java.util.List;

public record PermissionTreeNode(Integer id, String name, String description,
        List<PermissionTreeNode> children, boolean hasPermission) {
    public PermissionTreeNode {
        if (children == null) {
            children = new ArrayList<>();
        }
    }

    public PermissionTreeNode(Integer id, String name, String description) {
        this(id, name, description, new ArrayList<>(), false);
    }
}

