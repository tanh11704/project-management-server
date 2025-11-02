package com.skytech.projectmanagement.user.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import com.skytech.projectmanagement.user.entity.PasswordResetToken;
import com.skytech.projectmanagement.user.entity.User;
import com.skytech.projectmanagement.user.repository.PasswordResetTokenRepository;
import com.skytech.projectmanagement.user.service.PasswordResetTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
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
}
