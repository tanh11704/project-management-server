package com.skytech.projectmanagement.auth.service;

import java.util.List;
import java.util.Set;
import com.skytech.projectmanagement.auth.dto.PermissionTreeNode;

public interface PermissionService {

    List<PermissionTreeNode> buildPermissionTree(Set<String> userPermissionNames);

    List<PermissionTreeNode> buildFullPermissionTree();

    Set<String> getEffectivePermissions(Set<String> userPermissionNames);

    Set<Integer> filterLeafPermissionIds(Set<Integer> permissionIds);
}

