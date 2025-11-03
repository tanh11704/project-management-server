package com.skytech.projectmanagement.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @NotBlank(message = "Refresh token không được để trống") String refreshToken,
        @NotBlank(message = "Access token không được để trống") String accessToken) {
}
