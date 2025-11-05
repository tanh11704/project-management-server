package com.skytech.projectmanagement.user.dto;

import com.skytech.projectmanagement.common.validation.Password;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request đổi mật khẩu của người dùng")
public record ChangePasswordRequest(@Schema(
        description = "Mật khẩu hiện tại (có thể là mật khẩu thường hoặc mật khẩu tạm thời từ email)",
        example = "OldPassword123!",
        required = true) @NotBlank(message = "Mật khẩu cũ không được để trống") String oldPassword,

        @Schema(description = """
                Mật khẩu mới. Phải đáp ứng yêu cầu:
                - Ít nhất 8 ký tự
                - Có ít nhất 1 chữ hoa
                - Có ít nhất 1 chữ thường
                - Có ít nhất 1 số
                - Có ít nhất 1 ký tự đặc biệt
                - Không được trùng với mật khẩu cũ
                """, example = "NewPassword123!", required = true, minLength = 8) @NotBlank(
                message = "Mật khẩu mới không được để trống") @Password String newPassword) {

}
