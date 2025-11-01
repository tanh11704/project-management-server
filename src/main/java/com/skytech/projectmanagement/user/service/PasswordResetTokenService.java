package com.skytech.projectmanagement.user.service;

import com.skytech.projectmanagement.user.entity.PasswordResetToken;
import com.skytech.projectmanagement.user.entity.User;

public interface PasswordResetTokenService {

    String createResetToken(User user);

    void deleteToken(PasswordResetToken token);
}
