package com.skytech.projectmanagement.notification.config;

import com.skytech.projectmanagement.auth.security.JwtTokenProvider;
import com.skytech.projectmanagement.user.repository.UserRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketJwtInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = getTokenFromHeader(accessor);

            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                String email = jwtTokenProvider.getEmail(token);

                userRepository.findByEmail(email).ifPresent(user -> {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null,
                                    userDetails.getAuthorities());
                    accessor.setUser(authentication);
                    log.info("WebSocket connection authenticated for user: {}", email);
                });
            } else {
                log.warn("WebSocket connection rejected: Invalid or missing JWT token");
                throw new RuntimeException("Invalid or missing JWT token");
            }
        }

        return message;
    }

    private String getTokenFromHeader(StompHeaderAccessor accessor) {
        String token = accessor.getFirstNativeHeader("Authorization");

        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            return token.substring(7);
        }

        String queryString = accessor.getSessionAttributes() != null
                ? (String) accessor.getSessionAttributes().get("query")
                : null;

        if (queryString != null && queryString.contains("token=")) {
            int tokenStart = queryString.indexOf("token=") + 6;
            int tokenEnd = queryString.indexOf("&", tokenStart);
            if (tokenEnd == -1) {
                tokenEnd = queryString.length();
            }
            return queryString.substring(tokenStart, tokenEnd);
        }

        return null;
    }
}

