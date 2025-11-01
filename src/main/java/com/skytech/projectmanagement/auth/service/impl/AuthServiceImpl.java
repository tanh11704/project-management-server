package com.skytech.projectmanagement.auth.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import com.skytech.projectmanagement.auth.dto.LoginRequest;
import com.skytech.projectmanagement.auth.dto.LoginResponse;
import com.skytech.projectmanagement.auth.dto.RefreshTokenRequest;
import com.skytech.projectmanagement.auth.dto.RefreshTokenResponse;
import com.skytech.projectmanagement.auth.dto.UserLoginResponse;
import com.skytech.projectmanagement.auth.security.JwtTokenProvider;
import com.skytech.projectmanagement.auth.service.AuthService;
import com.skytech.projectmanagement.common.mail.EmailService;
import com.skytech.projectmanagement.user.entity.User;
import com.skytech.projectmanagement.user.entity.UserRefreshToken;
import com.skytech.projectmanagement.user.service.PasswordResetTokenService;
import com.skytech.projectmanagement.user.service.RefreshTokenService;
import com.skytech.projectmanagement.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final EmailService emailService;
    private final long jwtExpirationMs;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider, UserService userService,
            RefreshTokenService refreshTokenService,
            PasswordResetTokenService passwordResetTokenService, EmailService emailService,
            @Value("${jwt.expiration-ms}") long jwtExpirationMs) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
        this.passwordResetTokenService = passwordResetTokenService;
        this.emailService = emailService;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                        loginRequest.email(), loginRequest.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.generateJwtToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        User user = userService.findUserByEmail(loginRequest.email());

        refreshTokenService.saveRefreshToken(user.getId(), refreshToken,
                jwtTokenProvider.getExpirationDateFromToken(refreshToken).toInstant(),
                loginRequest.deviceInfo());

        List<String> permissions = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        UserLoginResponse userResponse = new UserLoginResponse(user.getId(), user.getFullName(),
                user.getEmail(), user.getAvatar(), permissions);

        return new LoginResponse(userResponse, accessToken, refreshToken, jwtExpirationMs / 1000);
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.deleteRefreshToken(refreshToken);
    }

    @Override
    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        String oldRefreshToken = request.refreshToken();

        UserRefreshToken tokenEntity =
                refreshTokenService.verifyAndRotateRefreshToken(oldRefreshToken);

        Integer userId = tokenEntity.getUserId();
        User user = userService.findUserById(userId);

        String newAccessToken = jwtTokenProvider.generateJwtToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        refreshTokenService.saveRefreshToken(user.getId(), newRefreshToken,
                jwtTokenProvider.getExpirationDateFromToken(newRefreshToken).toInstant(),
                tokenEntity.getDeviceInfo());

        return new RefreshTokenResponse(newAccessToken, newRefreshToken, jwtExpirationMs / 1000);
    }

    @Override
    public void handleForgotPassword(String email) {
        try {
            User user = userService.findUserByEmail(email);

            String rawTempPassword = passwordResetTokenService.createResetToken(user);

            emailService.sendPasswordResetEmail(user.getEmail(), rawTempPassword);

        } catch (Exception e) {
            log.warn(
                    "Yêu cầu reset password cho email '{}' thất bại hoặc email không tồn tại. Lỗi: {}",
                    email, e.getMessage());
        }
    }

}
