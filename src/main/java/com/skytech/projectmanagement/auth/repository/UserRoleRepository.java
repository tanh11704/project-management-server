package com.skytech.projectmanagement.auth.repository;

import com.skytech.projectmanagement.auth.entity.UserRole;
import com.skytech.projectmanagement.auth.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    @Transactional
    void deleteById_UserId(Integer userId);

    long countById_RoleId(Integer roleId);
}
