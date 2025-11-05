package com.skytech.projectmanagement.user.dto;

import java.time.Instant;
import com.skytech.projectmanagement.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thông tin người dùng trong hệ thống")
public record UserResponse(
        @Schema(description = "ID của người dùng", example = "1", required = true) Integer id,

        @Schema(description = "Tên đầy đủ của người dùng", example = "Nguyễn Văn A",
                required = true) String fullName,

        @Schema(description = "Email của người dùng", example = "user@example.com",
                required = true) String email,

        @Schema(description = "URL ảnh đại diện của người dùng",
                example = "https://res.cloudinary.com/example/image/upload/v1234567890/avatar.jpg",
                nullable = true) String avatarUrl,

        @Schema(description = "Thời điểm tạo tài khoản", example = "2024-01-01T00:00:00Z",
                required = true) Instant createdAt) {

    public static UserResponse fromEntity(User user) {

        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getAvatar(),
                user.getCreatedAt());
    }
}
