package com.skytech.projectmanagement.auth.security;

import java.time.Instant;
import java.util.Optional;
import com.skytech.projectmanagement.user.entity.PasswordResetToken;
import com.skytech.projectmanagement.user.repository.PasswordResetTokenRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository resetTokenRepository;

    @Override
    @Transactional
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        String email = authentication.getName();
        String rawPassword = authentication.getCredentials().toString();

        CustomUserDetails userDetails =
                (CustomUserDetails) userDetailsService.loadUserByUsername(email);

        Integer userId = userDetails.getUserEntity().getId();

        // 2. KIỂM TRA (A): MẬT KHẨU CHÍNH
        if (passwordEncoder.matches(rawPassword, userDetails.getPassword())) {
            // Xác thực thành công bằng mật khẩu chính
            return new UsernamePasswordAuthenticationToken(userDetails, null,
                    userDetails.getAuthorities());
        }

        // 3. KIỂM TRA (B): MẬT KHẨU TẠM THỜI (Đã sửa logic)
        Optional<PasswordResetToken> tempTokenOpt = resetTokenRepository.findByUserId(userId);
        if (tempTokenOpt.isPresent()) {
            PasswordResetToken token = tempTokenOpt.get();

            if (passwordEncoder.matches(rawPassword, token.getHashToken())) {
                if (token.getExpiresAt().isAfter(Instant.now())) {
                    // Không xóa token ngay - để user có thể dùng nó để đổi mật khẩu
                    // Token sẽ được xóa khi user đổi mật khẩu thành công
                    return new UsernamePasswordAuthenticationToken(userDetails, null,
                            userDetails.getAuthorities());
                } else {
                    // Chỉ xóa token nếu đã hết hạn
                    resetTokenRepository.delete(token);
                }
            }
        }

        throw new BadCredentialsException("Email hoặc mật khẩu không đúng.");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
