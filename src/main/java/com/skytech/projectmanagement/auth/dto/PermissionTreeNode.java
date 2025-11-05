package com.skytech.projectmanagement.auth.dto;

import java.util.ArrayList;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Node trong cây phân quyền, đại diện cho một permission và các permission con của nó")
public record PermissionTreeNode(
        @Schema(description = "ID của permission", example = "1", required = true) Integer id,

        @Schema(description = "Tên của permission (mã định danh)", example = "USER_READ",
                required = true) String name,

        @Schema(description = "Mô tả chi tiết về permission", example = "Xem thông tin người dùng",
                required = true) String description,

        @Schema(description = "Danh sách các permission con (sub-permissions)",
                required = true) List<PermissionTreeNode> children,

        @Schema(description = "Cho biết người dùng có permission này hay không", example = "true",
                required = true) boolean hasPermission) {
    public PermissionTreeNode {
        if (children == null) {
            children = new ArrayList<>();
        }
    }

    public PermissionTreeNode(Integer id, String name, String description) {
        this(id, name, description, new ArrayList<>(), false);
    }
}

