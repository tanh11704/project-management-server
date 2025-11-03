package com.skytech.projectmanagement.user.service;

import java.time.Instant;
import com.skytech.projectmanagement.user.entity.UserRefreshToken;

public interface RefreshTokenService {
    void saveRefreshToken(Integer userId, String token, Instant expiryDate, String ipAddress);

    void deleteRefreshToken(String token);

    UserRefreshToken verifyAndRotateRefreshToken(String oldToken);

    void purgeExpiredRefreshTokens();
}
