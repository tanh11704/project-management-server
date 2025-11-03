package com.skytech.projectmanagement.user.dto;

import com.skytech.projectmanagement.common.validation.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank(message = "Tên đầy đủ không được để trống") String fullName,

        @NotBlank(message = "Email không được để trống") @Email(
                message = "Email không đúng định dạng") String email,

        @NotBlank(message = "Mật khẩu không được để trống") @Password String password) {

}
