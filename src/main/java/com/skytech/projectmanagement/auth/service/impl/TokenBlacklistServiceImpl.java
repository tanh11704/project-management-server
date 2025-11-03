package com.skytech.projectmanagement.auth.service.impl;

import java.time.Instant;
import com.skytech.projectmanagement.auth.entity.BlacklistedToken;
import com.skytech.projectmanagement.auth.repository.BlacklistedTokenRepository;
import com.skytech.projectmanagement.auth.service.TokenBlacklistService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final BlacklistedTokenRepository tokenRepository;

    @Override
    @Transactional
    public void blacklistToken(String token, Instant expiresAt) {
        // Kiểm tra xem token đã có trong blacklist chưa
        if (tokenRepository.findByToken(token).isPresent()) {
            log.debug("Token đã có trong blacklist: {}",
                    token.substring(0, Math.min(10, token.length())));
            return;
        }

        BlacklistedToken blacklistedToken = new BlacklistedToken();
        blacklistedToken.setToken(token);
        blacklistedToken.setExpiresAt(expiresAt);
        tokenRepository.save(blacklistedToken);

        log.debug("Đã thêm token vào blacklist, expires at: {}", expiresAt);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTokenBlacklisted(String token) {
        return tokenRepository.findByToken(token).isPresent();
    }

    @Override
    @Scheduled(cron = "0 0 2 * * ?") // Chạy mỗi ngày lúc 2:00 AM
    @Transactional
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        log.info("CRON JOB: Bắt đầu dọn dẹp blacklisted tokens hết hạn trước {}", now);

        tokenRepository.deleteExpiredTokens(now);

        log.info("CRON JOB: Đã dọn dẹp blacklisted tokens hết hạn xong");
    }
}

