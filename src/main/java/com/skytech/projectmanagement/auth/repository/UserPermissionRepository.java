package com.skytech.projectmanagement.auth.repository;

import com.skytech.projectmanagement.auth.entity.UserPermission;
import com.skytech.projectmanagement.auth.entity.UserPermissionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPermissionRepository extends JpaRepository<UserPermission, UserPermissionId> {

    void deleteById_UserId(Integer userId);

    boolean existsById_UserIdAndId_PermissionId(Integer userId, Integer permissionId);
}

