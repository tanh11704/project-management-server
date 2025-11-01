package com.skytech.projectmanagement.auth.controller;

import com.skytech.projectmanagement.auth.dto.ForgotPasswordRequest;
import com.skytech.projectmanagement.auth.dto.LoginRequest;
import com.skytech.projectmanagement.auth.dto.LoginResponse;
import com.skytech.projectmanagement.auth.dto.LogoutRequest;
import com.skytech.projectmanagement.auth.dto.RefreshTokenRequest;
import com.skytech.projectmanagement.auth.dto.RefreshTokenResponse;
import com.skytech.projectmanagement.auth.service.AuthService;
import com.skytech.projectmanagement.common.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth-service/v1")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse loginData = authService.login(loginRequest);

        SuccessResponse<LoginResponse> response =
                SuccessResponse.of(loginData, "Đăng nhập thành công.");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<Object>> logout(
            @Valid @RequestBody LogoutRequest logoutRequest) {
        authService.logout(logoutRequest.refreshToken());

        SuccessResponse<Object> response = SuccessResponse.of(null, "Đăng xuất thành công.");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<SuccessResponse<RefreshTokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse responseData = authService.refreshToken(request);

        SuccessResponse<RefreshTokenResponse> response =
                SuccessResponse.of(responseData, "Làm mới token thành công.");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<SuccessResponse<Object>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.handleForgotPassword(request.email());

        SuccessResponse<Object> response = SuccessResponse.of(null,
                "Nếu email của bạn tồn tại trong hệ thống, bạn sẽ nhận được một hướng dẫn đặt lại mật khẩu.");

        return ResponseEntity.ok(response);
    }

}
