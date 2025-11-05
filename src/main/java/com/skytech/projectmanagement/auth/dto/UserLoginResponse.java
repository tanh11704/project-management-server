package com.skytech.projectmanagement.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thông tin người dùng sau khi đăng nhập thành công")
public record UserLoginResponse(
        @Schema(description = "ID của người dùng", example = "1", required = true) Integer id,

        @Schema(description = "Tên đầy đủ của người dùng", example = "Nguyễn Văn A",
                required = true) String fullName,

        @Schema(description = "Email của người dùng", example = "user@example.com",
                required = true) String email,

        @Schema(description = "URL ảnh đại diện của người dùng",
                example = "https://res.cloudinary.com/example/image/upload/v1234567890/avatar.jpg",
                nullable = true) String avatar) {
}
