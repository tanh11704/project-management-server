package com.skytech.projectmanagement.user.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import com.skytech.projectmanagement.user.entity.PasswordResetToken;
import com.skytech.projectmanagement.user.entity.User;
import com.skytech.projectmanagement.user.repository.PasswordResetTokenRepository;
import com.skytech.projectmanagement.user.service.PasswordResetTokenService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public String createResetToken(User user) {
        tokenRepository.deleteByUserId(user.getId());

        String rawTempPassword = UUID.randomUUID().toString().substring(0, 8);
        Instant expiresAt = Instant.now().plus(1, ChronoUnit.HOURS);

        String hashedTempPassword = passwordEncoder.encode(rawTempPassword);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUserId(user.getId());
        resetToken.setHashToken(hashedTempPassword);
        resetToken.setExpiresAt(expiresAt);

        tokenRepository.save(resetToken);

        return rawTempPassword;
    }

    @Override
    @Transactional
    public void deleteToken(PasswordResetToken token) {
        tokenRepository.delete(token);
    }

    /**
     * Dọn dẹp các password reset tokens đã hết hạn Chạy mỗi ngày lúc 4:00 AM
     */
    @Scheduled(cron = "0 0 4 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        log.info("CRON JOB: Bắt đầu cleanup password reset tokens hết hạn trước {}", now);

        // Xóa các token đã hết hạn
        List<PasswordResetToken> expiredTokens = tokenRepository.findAll().stream()
                .filter(token -> token.getExpiresAt().isBefore(now)).toList();

        if (!expiredTokens.isEmpty()) {
            tokenRepository.deleteAll(expiredTokens);
            log.info("CRON JOB: Đã xóa {} password reset tokens hết hạn", expiredTokens.size());
        } else {
            log.info("CRON JOB: Không có password reset token nào hết hạn để xóa");
        }
    }
}
