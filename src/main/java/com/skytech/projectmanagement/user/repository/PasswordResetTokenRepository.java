package com.skytech.projectmanagement.user.repository;

import java.util.Optional;
import com.skytech.projectmanagement.user.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {

    void deleteByUserId(Integer userId);

    Optional<PasswordResetToken> findByUserId(Integer userId);
}
