package com.skytech.projectmanagement.user.dto;

import com.skytech.projectmanagement.common.validation.Password;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "Mật khẩu cũ không được để trống") String oldPassword,

        @NotBlank(message = "Mật khẩu mới không được để trống") @Password String newPassword) {

}
