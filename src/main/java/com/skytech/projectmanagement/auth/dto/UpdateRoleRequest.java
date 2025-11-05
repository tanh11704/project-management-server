package com.skytech.projectmanagement.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateRoleRequest(
        @NotBlank(message = "Tên vai trò không được để trống") @Size(max = 50,
                message = "Tên vai trò không được vượt quá 50 ký tự") @Pattern(regexp = "^[A-Z_]+$",
                        message = "Tên vai trò chỉ được chứa chữ hoa và gạch dưới (_)") String name,

        String description) {

}

