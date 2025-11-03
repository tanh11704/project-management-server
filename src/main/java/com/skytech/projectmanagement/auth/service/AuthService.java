package com.skytech.projectmanagement.auth.service;

import com.skytech.projectmanagement.auth.dto.LoginRequest;
import com.skytech.projectmanagement.auth.dto.LoginResponse;
import com.skytech.projectmanagement.auth.dto.RefreshTokenRequest;
import com.skytech.projectmanagement.auth.dto.RefreshTokenResponse;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest, String ipAddress);

    void logout(String refreshToken, String accessToken);

    RefreshTokenResponse refreshToken(RefreshTokenRequest request);

    void handleForgotPassword(String email);

}
