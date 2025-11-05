package com.skytech.projectmanagement.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response đăng nhập thành công, chứa thông tin người dùng và tokens")
public record LoginResponse(
        @Schema(description = "Thông tin người dùng đã đăng nhập",
                required = true) UserLoginResponse user,

        @Schema(description = "Access token để xác thực các request tiếp theo",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                required = true) String accessToken,

        @Schema(description = "Refresh token để làm mới access token khi hết hạn",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                required = true) String refreshToken,

        @Schema(description = "Thời gian hết hạn của access token (tính bằng giây)",
                example = "900", required = true) long expiresIn) {

}
