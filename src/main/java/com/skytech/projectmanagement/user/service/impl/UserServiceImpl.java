package com.skytech.projectmanagement.user.service.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.skytech.projectmanagement.common.dto.PaginatedResponse;
import com.skytech.projectmanagement.common.dto.Pagination;
import com.skytech.projectmanagement.common.exception.EmailExistsException;
import com.skytech.projectmanagement.common.exception.FileStorageException;
import com.skytech.projectmanagement.common.exception.InvalidOldPasswordException;
import com.skytech.projectmanagement.common.exception.ResourceNotFoundException;
import com.skytech.projectmanagement.common.exception.UserNotFoundInRequestException;
import com.skytech.projectmanagement.common.exception.ValidationException;
import com.skytech.projectmanagement.common.mail.EmailService;
import com.skytech.projectmanagement.filestorage.service.FileStorageService;
import com.skytech.projectmanagement.user.dto.ChangePasswordRequest;
import com.skytech.projectmanagement.user.dto.CreateUserRequest;
import com.skytech.projectmanagement.user.dto.ResetPasswordsRequest;
import com.skytech.projectmanagement.user.dto.ResetPasswordsResponse;
import com.skytech.projectmanagement.user.dto.UpdateUserRequest;
import com.skytech.projectmanagement.user.dto.UserResponse;
import com.skytech.projectmanagement.user.entity.PasswordResetToken;
import com.skytech.projectmanagement.user.entity.User;
import com.skytech.projectmanagement.user.repository.PasswordResetTokenRepository;
import com.skytech.projectmanagement.user.repository.UserRefreshTokenRepository;
import com.skytech.projectmanagement.user.repository.UserRepository;
import com.skytech.projectmanagement.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException(
                "Không tìm thấy người dùng với email: " + email));
    }

    @Override
    public User findUserById(Integer id) {
        return userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));
    }

    @Override
    @Transactional
    public void updatePassword(Integer userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user"));

        user.setHashPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(String userEmail, ChangePasswordRequest request) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng."));

        // Kiểm tra mật khẩu cũ: có thể là mật khẩu chính hoặc mật khẩu tạm thời
        boolean isOldPasswordValid =
                passwordEncoder.matches(request.oldPassword(), currentUser.getHashPassword());

        // Nếu không khớp với mật khẩu chính, kiểm tra mật khẩu tạm thời
        if (!isOldPasswordValid) {
            var tempTokenOpt = passwordResetTokenRepository.findByUserId(currentUser.getId());
            if (tempTokenOpt.isPresent()) {
                PasswordResetToken token = tempTokenOpt.get();
                if (token.getExpiresAt().isAfter(Instant.now())) {
                    // Kiểm tra mật khẩu tạm thời
                    if (passwordEncoder.matches(request.oldPassword(), token.getHashToken())) {
                        isOldPasswordValid = true;
                        // Xóa token tạm thời sau khi xác thực thành công
                        passwordResetTokenRepository.delete(token);
                    }
                } else {
                    // Token đã hết hạn, xóa nó
                    passwordResetTokenRepository.delete(token);
                }
            }
        } else {
            // Nếu dùng mật khẩu chính hợp lệ, cũng xóa token tạm thời nếu có
            passwordResetTokenRepository.deleteByUserId(currentUser.getId());
        }

        if (!isOldPasswordValid) {
            throw new InvalidOldPasswordException(
                    "Mật khẩu cũ bạn đã nhập không khớp với mật khẩu hiện tại hoặc mật khẩu tạm thời.");
        }

        // Kiểm tra mật khẩu mới không được trùng với mật khẩu cũ
        if (passwordEncoder.matches(request.newPassword(), currentUser.getHashPassword())) {
            throw new ValidationException(
                    "Mật khẩu mới không được trùng với mật khẩu hiện tại. Vui lòng chọn mật khẩu khác.");
        }

        // Cập nhật mật khẩu mới
        currentUser.setHashPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(currentUser);

        // Xóa tất cả refresh tokens để đăng xuất khỏi các thiết bị khác
        userRefreshTokenRepository.deleteByUserId(currentUser.getId());
    }

    @Override
    public User getUserEntityById(Integer userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy user với ID: " + userId));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserProfile(String email) {
        User user = findUserByEmail(email);

        return createResponseWithAvatarUrl(user);
    }

    @Override
    public PaginatedResponse<UserResponse> getUsers(Pageable pageable, String search) {
        Specification<User> spec = (root, query, cb) -> {
            if (!StringUtils.hasText(search)) {
                return cb.conjunction();
            }

            Predicate nameLike =
                    cb.like(cb.lower(root.get("fullName")), "%" + search.toLowerCase() + "%");
            Predicate emailLike =
                    cb.like(cb.lower(root.get("email")), "%" + search.toLowerCase() + "%");

            return cb.or(nameLike, emailLike);
        };

        Page<User> userPage = userRepository.findAll(spec, pageable);

        List<UserResponse> userDtoList =
                userPage.stream().map(this::createResponseWithAvatarUrl).toList();

        Pagination pagination = new Pagination(userPage.getNumber(), userPage.getSize(),
                userPage.getTotalElements(), userPage.getTotalPages());

        return PaginatedResponse.of(userDtoList, pagination,
                "Lấy danh sách người dùng thành công.");
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(existingUser -> {
            throw new EmailExistsException("Email '" + request.email() + "' đã được sử dụng.");
        });

        User newUser = new User();
        newUser.setFullName(request.fullName());
        newUser.setEmail(request.email());
        newUser.setHashPassword(passwordEncoder.encode(request.password()));

        User savedUser = userRepository.save(newUser);

        return UserResponse.fromEntity(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        return createResponseWithAvatarUrl(user);
    }

    @Override
    public UserResponse updateUser(Integer userId, UpdateUserRequest request) {
        User userToUpdate = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        if (request.fullName() != null) {
            userToUpdate.setFullName(request.fullName());
        }

        User updatedUser = userRepository.save(userToUpdate);

        return createResponseWithAvatarUrl(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId);
        }

        User userToDelete = userRepository.findById(userId).get();

        String oldObjectName = userToDelete.getAvatar();
        if (oldObjectName != null && !oldObjectName.isBlank()) {
            try {
                log.info("Attempting to delete old avatar: {}", oldObjectName);
                fileStorageService.deleteFile(oldObjectName);
            } catch (Exception e) {
                log.error("Could not delete old avatar '{}': {}", oldObjectName, e.getMessage());
            }
        }

        // Kiểm tra xem user có đang là thành viên của project nào không (Module Project)
        // Xóa user khỏi team (Module Team)
        // Xóa user khỏi role_permission

        userRefreshTokenRepository.deleteByUserId(userId);

        userRepository.deleteById(userId);
    }

    @Override
    @Transactional
    public UserResponse uploadAvatar(String userEmail, MultipartFile file) {
        validateAvatarFile(file);

        User currentUser = findUserByEmail(userEmail);

        String oldObjectName = currentUser.getAvatar();
        if (oldObjectName != null && !oldObjectName.isBlank()) {
            try {
                log.info("Attempting to delete old avatar: {}", oldObjectName);
                fileStorageService.deleteFile(oldObjectName);
            } catch (Exception e) {
                log.error("Could not delete old avatar '{}': {}", oldObjectName, e.getMessage());
            }
        }

        String objectName = fileStorageService.uploadFile(file, "avatars/");

        currentUser.setAvatar(objectName);
        User updatedUser = userRepository.save(currentUser);

        return createResponseWithAvatarUrl(updatedUser);
    }

    private void validateAvatarFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("File avatar không được để trống.");
        }
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/jpeg")
                && !contentType.startsWith("image/png"))) {
            throw new FileStorageException("Chỉ chấp nhận file ảnh JPG hoặc PNG.");
        }
    }

    @Override
    public Map<Integer, User> findUsersMapByIds(Set<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<User> users = userRepository.findByIdIn(userIds);
        return users.stream().collect(Collectors.toMap(User::getId, Function.identity()));
    }

    @Override
    @Transactional
    public void validateUsersExist(Set<Integer> userIds) {
        long foundCount = userRepository.countByIdIn(userIds);
        if (foundCount != userIds.size()) {
            List<User> foundUsers = userRepository.findByIdIn(userIds);
            Set<Integer> foundIds =
                    foundUsers.stream().map(User::getId).collect(Collectors.toSet());
            Set<Integer> missingIds = new java.util.HashSet<>(userIds);
            missingIds.removeAll(foundIds);

            throw new UserNotFoundInRequestException("Các User ID không tồn tại: " + missingIds);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Integer userId) {
        return userRepository.existsById(userId);
    }

    @Override
    @Transactional
    public ResetPasswordsResponse resetPasswordsForUsers(ResetPasswordsRequest request) {
        List<Integer> resetUserIds = new java.util.ArrayList<>();
        List<Integer> failedUserIds = new java.util.ArrayList<>();

        for (Integer userId : request.getUserIds()) {
            try {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Không tìm thấy người dùng với ID: " + userId));

                // Generate random password (12 characters: uppercase, lowercase, numbers)
                String newPassword = generateRandomPassword();
                String encodedPassword = passwordEncoder.encode(newPassword);

                // Update password
                user.setHashPassword(encodedPassword);
                userRepository.save(user);

                // Invalidate all refresh tokens for this user
                userRefreshTokenRepository.deleteByUserId(userId);

                // Send email with new password
                try {
                    emailService.sendNewPasswordEmail(user.getEmail(), user.getFullName(),
                            newPassword);
                    log.info("Đã gửi email mật khẩu mới tới user ID: {} ({})", userId,
                            user.getEmail());
                } catch (Exception emailException) {
                    log.error("Đã reset mật khẩu cho user ID {} nhưng không thể gửi email: {}",
                            userId, emailException.getMessage());
                    // Still count as success since password was reset
                }

                resetUserIds.add(userId);
                log.info("Đã reset mật khẩu cho user ID: {}", userId);
            } catch (Exception e) {
                failedUserIds.add(userId);
                log.error("Không thể reset mật khẩu cho user ID {}: {}", userId, e.getMessage());
            }
        }

        return new ResetPasswordsResponse(resetUserIds, failedUserIds, resetUserIds.size(),
                failedUserIds.size());
    }

    private String generateRandomPassword() {
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        // Ký tự đặc biệt an toàn cho mật khẩu
        String specialChars = "@$!%*?&#^()_+-=[]{};':\",./<>?|\\~`";
        String allChars = uppercase + lowercase + numbers + specialChars;

        java.util.Random random = new java.util.Random();
        StringBuilder password = new StringBuilder(12);

        // Ensure at least one character from each set (uppercase, lowercase, number, special)
        password.append(uppercase.charAt(random.nextInt(uppercase.length())));
        password.append(lowercase.charAt(random.nextInt(lowercase.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));

        // Fill the rest randomly (12 - 4 = 8 more characters)
        for (int i = 4; i < 12; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // Shuffle the password
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }

    private UserResponse createResponseWithAvatarUrl(User user) {
        String objectName = user.getAvatar();
        String finalAvatarUrl = null;

        if (objectName != null && !objectName.isBlank()) {
            finalAvatarUrl = fileStorageService.getFileUrl(objectName);
        }

        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), finalAvatarUrl,
                user.getCreatedAt());
    }
}
