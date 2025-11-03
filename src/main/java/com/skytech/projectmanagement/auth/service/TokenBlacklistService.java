package com.skytech.projectmanagement.auth.service;

import java.time.Instant;

public interface TokenBlacklistService {
    void blacklistToken(String token, Instant expiresAt);

    boolean isTokenBlacklisted(String token);

    void cleanupExpiredTokens();
}

