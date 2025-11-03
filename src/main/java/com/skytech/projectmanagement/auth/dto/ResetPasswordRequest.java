package com.skytech.projectmanagement.auth.dto;

import com.skytech.projectmanagement.common.validation.Password;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(@NotBlank(message = "Token không được để trống") String token,

        @NotBlank(message = "Mật khẩu mới không được để trống") @Password String newPassword) {

}
