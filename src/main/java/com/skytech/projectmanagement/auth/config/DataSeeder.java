package com.skytech.projectmanagement.auth.config;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.skytech.projectmanagement.auth.entity.Permission;
import com.skytech.projectmanagement.auth.repository.PermissionRepository;
import com.skytech.projectmanagement.user.entity.User;
import com.skytech.projectmanagement.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {
    @Value("${seeder.admin.email}")
    private String adminEmail;

    @Value("${seeder.admin.password}")
    private String adminPassword;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Bắt đầu quá trình seeding dữ liệu...");

        seedPermissions();

        seedAdminUser();

        log.info("Quá trình seeding dữ liệu hoàn tất.");
    }

    private void seedPermissions() {
        log.info("Bắt đầu seeding permissions...");
        Map<String, Permission> permissions = new HashMap<>();

        // Tạo các permissions cha (parent permissions) trước
        Permission userManage = findOrCreatePermission(null, "USER_MANAGE",
                "Quyền quản lý người dùng (Tạo/Sửa/Xóa bất kỳ người dùng nào)");
        Permission projectManage =
                findOrCreatePermission(null, "PROJECT_MANAGE", "Quyền quản lý dự án");
        Permission taskManage = findOrCreatePermission(null, "TASK_MANAGE", "Quyền quản lý task");
        Permission bugManage = findOrCreatePermission(null, "BUG_MANAGE", "Quyền quản lý bug");

        permissions.put("USER_MANAGE", userManage);
        permissions.put("PROJECT_MANAGE", projectManage);
        permissions.put("TASK_MANAGE", taskManage);
        permissions.put("BUG_MANAGE", bugManage);

        // USER_MANAGE children
        permissions.put("USER_READ", findOrCreatePermission(userManage, "USER_READ",
                "Quyền xem danh sách tất cả người dùng (GET /users)"));
        permissions.put("USER_CREATE",
                findOrCreatePermission(userManage, "USER_CREATE", "Quyền tạo người dùng mới"));
        permissions.put("USER_UPDATE", findOrCreatePermission(userManage, "USER_UPDATE",
                "Quyền cập nhật thông tin người dùng"));
        permissions.put("USER_DELETE",
                findOrCreatePermission(userManage, "USER_DELETE", "Quyền xóa người dùng"));

        // PROJECT_MANAGE children
        permissions.put("PROJECT_CREATE", findOrCreatePermission(projectManage, "PROJECT_CREATE",
                "Quyền tạo dự án mới (POST /projects)"));
        permissions.put("PROJECT_READ_ALL", findOrCreatePermission(projectManage,
                "PROJECT_READ_ALL", "Quyền xem TẤT CẢ dự án (bỏ qua filter \"thành viên\")"));
        permissions.put("PROJECT_UPDATE", findOrCreatePermission(projectManage, "PROJECT_UPDATE",
                "Quyền cập nhật thông tin dự án"));
        permissions.put("PROJECT_DELETE",
                findOrCreatePermission(projectManage, "PROJECT_DELETE", "Quyền xóa dự án"));
        permissions.put("PROJECT_MANAGE_ANY", findOrCreatePermission(projectManage,
                "PROJECT_MANAGE_ANY", "Quyền Sửa/Xóa BẤT KỲ dự án nào (backward compatibility)"));
        permissions.put("PROJECT_MEMBER_MANAGE", findOrCreatePermission(projectManage,
                "PROJECT_MEMBER_MANAGE", "Quyền Thêm/Sửa/Xóa thành viên khỏi dự án"));

        // TASK_MANAGE children
        permissions.put("TASK_CREATE",
                findOrCreatePermission(taskManage, "TASK_CREATE", "Quyền tạo Task"));
        permissions.put("TASK_READ_ALL", findOrCreatePermission(taskManage, "TASK_READ_ALL",
                "Quyền xem tất cả Task (quyền PM/BA)"));
        permissions.put("TASK_UPDATE",
                findOrCreatePermission(taskManage, "TASK_UPDATE", "Quyền cập nhật Task"));
        permissions.put("TASK_DELETE",
                findOrCreatePermission(taskManage, "TASK_DELETE", "Quyền xóa Task"));

        // BUG_MANAGE children
        permissions.put("BUG_CREATE",
                findOrCreatePermission(bugManage, "BUG_CREATE", "Quyền tạo Bug"));
        permissions.put("BUG_READ_ALL",
                findOrCreatePermission(bugManage, "BUG_READ_ALL", "Quyền xem tất cả Bug"));
        permissions.put("BUG_UPDATE",
                findOrCreatePermission(bugManage, "BUG_UPDATE", "Quyền cập nhật Bug"));
        permissions.put("BUG_DELETE",
                findOrCreatePermission(bugManage, "BUG_DELETE", "Quyền xóa Bug"));

        // ROLE_MANAGE là leaf permission (không có parent)
        permissions.put("ROLE_MANAGE", findOrCreatePermission(null, "ROLE_MANAGE",
                "Quyền gán Role và Quyền trực tiếp cho user"));

        log.info("Đã seed {} permissions.", permissions.size());
    }

    private void seedAdminUser() {
        List<User> listUsers = userRepository.findByIsAdmin(true);

        if (listUsers.isEmpty()) {
            log.info("Tạo tài khoản Admin: {}", adminEmail);

            User adminUser = new User();
            adminUser.setEmail(adminEmail);
            adminUser.setFullName("Quản trị viên");
            adminUser.setHashPassword(passwordEncoder.encode(adminPassword));
            adminUser.setIsAdmin(true);
            adminUser.setCreatedAt(Instant.now());

            userRepository.save(adminUser);

            log.info("Tài khoản Admin đã được tạo.");
        } else {
            log.info("Tài khoản Admin ({}) đã tồn tại. Bỏ qua seeding.", adminEmail);
        }
    }

    private Permission findOrCreatePermission(Permission parent, String name, String description) {
        Permission existing = permissionRepository.findByName(name).orElse(null);

        if (existing != null) {
            // Update parent và description nếu khác với giá trị hiện tại
            boolean needsUpdate = false;

            if ((existing.getParent() == null && parent != null)
                    || (existing.getParent() != null && parent == null)
                    || (existing.getParent() != null && parent != null
                            && !existing.getParent().getId().equals(parent.getId()))) {
                existing.setParent(parent);
                needsUpdate = true;
            }

            if (description != null && !description.equals(existing.getDescription())) {
                existing.setDescription(description);
                needsUpdate = true;
            }

            if (needsUpdate) {
                log.info("Cập nhật Permission: {} (parent: {})", name,
                        parent != null ? parent.getName() : "null");
                return permissionRepository.save(existing);
            }

            return existing;
        }

        log.info("Tạo Permission: {} (parent: {})", name,
                parent != null ? parent.getName() : "null");
        Permission newPerm = new Permission();
        newPerm.setName(name);
        newPerm.setDescription(description);
        newPerm.setParent(parent);
        return permissionRepository.save(newPerm);
    }

}
