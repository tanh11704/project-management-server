package com.skytech.projectmanagement.auth.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.skytech.projectmanagement.auth.repository.PermissionRepository;
import com.skytech.projectmanagement.auth.security.CustomUserDetails;
import com.skytech.projectmanagement.common.exception.ResourceNotFoundException;
import com.skytech.projectmanagement.user.entity.User;
import com.skytech.projectmanagement.user.service.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user;
        try {
            user = userService.findUserByEmail(email);
        } catch (ResourceNotFoundException ex) {
            throw new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email);
        }

        Set<String> permissions;

        if (Boolean.TRUE.equals(user.getIsAdmin())) {
            permissions = permissionRepository.findAllPermissionNames();
        } else {
            permissions = permissionRepository.findHybridPermissionsByUserId(user.getId());
        }

        List<GrantedAuthority> authorities =
                permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

        return new CustomUserDetails(user, authorities);
    }

}
