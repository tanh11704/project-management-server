package com.skytech.projectmanagement.auth.repository;

import java.time.Instant;
import java.util.Optional;
import com.skytech.projectmanagement.auth.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Integer> {

    Optional<BlacklistedToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM BlacklistedToken b WHERE b.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") Instant now);
}

