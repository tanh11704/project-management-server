package com.skytech.projectmanagement.auth.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.skytech.projectmanagement.auth.dto.PermissionTreeNode;
import com.skytech.projectmanagement.auth.entity.Permission;
import com.skytech.projectmanagement.auth.repository.PermissionRepository;
import com.skytech.projectmanagement.auth.service.PermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PermissionTreeNode> buildPermissionTree(Set<String> userPermissionNames) {
        List<Permission> allPermissions = permissionRepository.findAll();

        // Get all effective permissions (including parent permissions if child is granted)
        Set<String> effectivePermissions = getEffectivePermissions(userPermissionNames);

        // Build tree structure
        Map<Integer, PermissionTreeNode> nodeMap = new HashMap<>();
        List<PermissionTreeNode> rootNodes = new ArrayList<>();

        // First pass: Create all nodes
        for (Permission perm : allPermissions) {
            PermissionTreeNode node =
                    new PermissionTreeNode(perm.getId(), perm.getName(), perm.getDescription(),
                            new ArrayList<>(), effectivePermissions.contains(perm.getName()));
            nodeMap.put(perm.getId(), node);
        }

        // Second pass: Build parent-child relationships
        for (Permission perm : allPermissions) {
            PermissionTreeNode node = nodeMap.get(perm.getId());
            if (perm.getParent() == null) {
                rootNodes.add(node);
            } else {
                PermissionTreeNode parentNode = nodeMap.get(perm.getParent().getId());
                if (parentNode != null) {
                    parentNode.children().add(node);
                }
            }
        }

        // Third pass: Propagate permission status from children to parents
        propagatePermissionToParents(nodeMap);

        // Rebuild rootNodes after propagation (since parent nodes may have been updated)
        List<PermissionTreeNode> updatedRootNodes = new ArrayList<>();
        for (Permission perm : allPermissions) {
            if (perm.getParent() == null) {
                PermissionTreeNode node = nodeMap.get(perm.getId());
                if (node != null) {
                    updatedRootNodes.add(node);
                }
            }
        }

        // Fourth pass: Filter tree to only include nodes with permission
        List<PermissionTreeNode> filteredRootNodes = updatedRootNodes.stream()
                .map(node -> filterTreeByPermission(node)).filter(node -> node != null).toList();

        // Sort root nodes and children by name for consistency
        filteredRootNodes.stream()
                .forEach(node -> node.children().sort((a, b) -> a.name().compareTo(b.name())));

        return filteredRootNodes;
    }

    private void propagatePermissionToParents(Map<Integer, PermissionTreeNode> nodeMap) {
        // Process all nodes, if a node has permission, mark all its parents
        for (PermissionTreeNode node : nodeMap.values()) {
            if (node.hasPermission()) {
                // Find all parent nodes and mark them
                markParentNodes(nodeMap, node);
            }
        }
    }

    private void markParentNodes(Map<Integer, PermissionTreeNode> nodeMap,
            PermissionTreeNode node) {
        List<Permission> allPermissions = permissionRepository.findAll();
        Permission permission = allPermissions.stream().filter(p -> p.getId().equals(node.id()))
                .findFirst().orElse(null);

        if (permission != null && permission.getParent() != null) {
            Integer parentId = permission.getParent().getId();
            PermissionTreeNode parentNode = nodeMap.get(parentId);
            if (parentNode != null && !parentNode.hasPermission()) {
                // Update parent node with hasPermission = true
                PermissionTreeNode updatedParent = new PermissionTreeNode(parentNode.id(),
                        parentNode.name(), parentNode.description(), parentNode.children(), true);
                nodeMap.put(parentId, updatedParent);
                markParentNodes(nodeMap, updatedParent); // Recursively mark grandparents
            }
        }
    }

    private PermissionTreeNode filterTreeByPermission(PermissionTreeNode node) {
        if (node == null) {
            return null;
        }

        // Filter children to only include nodes with permission
        List<PermissionTreeNode> filteredChildren = new ArrayList<>();
        for (PermissionTreeNode child : node.children()) {
            PermissionTreeNode filteredChild = filterTreeByPermission(child);
            if (filteredChild != null && filteredChild.hasPermission()) {
                filteredChildren.add(filteredChild);
            }
        }

        // If this node has permission OR has children with permission, include it
        if (node.hasPermission() || !filteredChildren.isEmpty()) {
            return new PermissionTreeNode(node.id(), node.name(), node.description(),
                    filteredChildren, node.hasPermission());
        }

        // If this node doesn't have permission and has no children with permission, exclude it
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionTreeNode> buildFullPermissionTree() {
        List<Permission> allPermissions = permissionRepository.findAll();

        Map<Integer, PermissionTreeNode> nodeMap = new HashMap<>();
        List<PermissionTreeNode> rootNodes = new ArrayList<>();

        // First pass: Create all nodes
        for (Permission perm : allPermissions) {
            PermissionTreeNode node = new PermissionTreeNode(perm.getId(), perm.getName(),
                    perm.getDescription(), new ArrayList<>(), false);
            nodeMap.put(perm.getId(), node);
        }

        // Second pass: Build parent-child relationships
        for (Permission perm : allPermissions) {
            PermissionTreeNode node = nodeMap.get(perm.getId());
            if (perm.getParent() == null) {
                rootNodes.add(node);
            } else {
                PermissionTreeNode parentNode = nodeMap.get(perm.getParent().getId());
                if (parentNode != null) {
                    parentNode.children().add(node);
                }
            }
        }

        rootNodes.sort((a, b) -> a.name().compareTo(b.name()));

        return rootNodes;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> getEffectivePermissions(Set<String> userPermissionNames) {
        if (userPermissionNames == null || userPermissionNames.isEmpty()) {
            return Set.of();
        }

        List<Permission> allPermissions = permissionRepository.findAll();
        Map<String, Permission> permissionMap =
                allPermissions.stream().collect(Collectors.toMap(Permission::getName, p -> p));

        Set<String> effectivePermissions = new java.util.HashSet<>(userPermissionNames);

        // For each user permission, add all parent permissions
        for (String permName : userPermissionNames) {
            Permission perm = permissionMap.get(permName);
            if (perm != null) {
                Permission current = perm;
                while (current.getParent() != null) {
                    effectivePermissions.add(current.getParent().getName());
                    current = current.getParent();
                }
            }
        }

        return effectivePermissions;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Integer> filterLeafPermissionIds(Set<Integer> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return Set.of();
        }

        List<Permission> allPermissions = permissionRepository.findAll();
        Map<Integer, Permission> permissionMap =
                allPermissions.stream().collect(Collectors.toMap(Permission::getId, p -> p));

        // Filter to only include leaf permissions (permissions with no children)
        return permissionIds.stream().filter(permId -> {
            Permission perm = permissionMap.get(permId);
            if (perm == null) {
                return false;
            }
            // Check if this permission has any children
            return allPermissions.stream()
                    .noneMatch(p -> p.getParent() != null && p.getParent().getId().equals(permId));
        }).collect(Collectors.toSet());
    }
}

