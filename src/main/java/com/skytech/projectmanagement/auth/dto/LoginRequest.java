package com.skytech.projectmanagement.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request đăng nhập vào hệ thống")
public record LoginRequest(
        @Schema(description = "Email đăng nhập của người dùng", example = "user@example.com",
                required = true) @NotBlank(message = "Email không được để trống") @Email(
                        message = "Email không đúng định dạng") String email,

        @Schema(description = "Mật khẩu của người dùng", example = "Password123!", required = true,
                minLength = 8) @NotBlank(
                        message = "Mật khẩu không được để trống") String password) {

}
