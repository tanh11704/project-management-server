package com.skytech.projectmanagement.user.dto;

import java.util.List;
import com.skytech.projectmanagement.auth.dto.PermissionTreeNode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thông tin profile đầy đủ của người dùng, bao gồm thông tin cơ bản và cây phân quyền")
public record UserProfileResponse(
        @Schema(description = "Thông tin cơ bản của người dùng", required = true) UserResponse user,

        @Schema(description = "Cây phân quyền của người dùng, hiển thị các permissions được cấp",
                required = true) List<PermissionTreeNode> permissionTree) {
}

