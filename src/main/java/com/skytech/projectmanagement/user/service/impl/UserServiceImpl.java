package com.skytech.projectmanagement.user.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.skytech.projectmanagement.auth.dto.PermissionTreeNode;
import com.skytech.projectmanagement.auth.repository.PermissionRepository;
import com.skytech.projectmanagement.auth.service.PermissionService;
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
import com.skytech.projectmanagement.user.dto.UserProfileResponse;
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
    private final PermissionRepository permissionRepository;
    private final PermissionService permissionService;

    @Override
    public User findUserByEmail(String email) {
        User user =
                userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy người dùng với email: " + email));

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng với email: " + email);
        }

        return user;
    }

    @Override
    public User findUserById(Integer id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id);
        }

        return user;
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

        boolean isOldPasswordValid =
                passwordEncoder.matches(request.oldPassword(), currentUser.getHashPassword());

        if (!isOldPasswordValid) {
            var tempTokenOpt = passwordResetTokenRepository.findByUserId(currentUser.getId());
            if (tempTokenOpt.isPresent()) {
                PasswordResetToken token = tempTokenOpt.get();
                if (token.getExpiresAt().isAfter(Instant.now())) {
                    if (passwordEncoder.matches(request.oldPassword(), token.getHashToken())) {
                        isOldPasswordValid = true;
                        passwordResetTokenRepository.delete(token);
                    }
                } else {
                    // Token đã hết hạn, xóa nó
                    passwordResetTokenRepository.delete(token);
                }
            }
        } else {
            passwordResetTokenRepository.deleteByUserId(currentUser.getId());
        }

        if (!isOldPasswordValid) {
            throw new InvalidOldPasswordException(
                    "Mật khẩu cũ bạn đã nhập không khớp với mật khẩu hiện tại hoặc mật khẩu tạm thời.");
        }

        if (passwordEncoder.matches(request.newPassword(), currentUser.getHashPassword())) {
            throw new ValidationException(
                    "Mật khẩu mới không được trùng với mật khẩu hiện tại. Vui lòng chọn mật khẩu khác.");
        }

        currentUser.setHashPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(currentUser);

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
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfileWithPermissions(String email) {
        User user = findUserByEmail(email);

        UserResponse userResponse = createResponseWithAvatarUrl(user);

        // Xây dựng cây phân quyền
        Set<String> userPermissionNames;
        if (Boolean.TRUE.equals(user.getIsAdmin())) {
            userPermissionNames = permissionRepository.findAllPermissionNames();
        } else {
            // Chỉ lấy các leaf permissions được lưu trong database
            userPermissionNames = permissionRepository.findLeafPermissionsByUserId(user.getId());
        }

        List<PermissionTreeNode> permissionTree =
                permissionService.buildPermissionTree(userPermissionNames);

        return new UserProfileResponse(userResponse, permissionTree);
    }

    @Override
    public PaginatedResponse<UserResponse> getUsers(Pageable pageable, String search,
            Boolean includeDeleted) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Chỉ lọc các user đã bị xóa nếu includeDeleted = false hoặc null
            if (!Boolean.TRUE.equals(includeDeleted)) {
                predicates.add(cb.or(cb.isNull(root.get("isDeleted")),
                        cb.equal(root.get("isDeleted"), false)));
            }

            if (StringUtils.hasText(search)) {
                Predicate nameLike =
                        cb.like(cb.lower(root.get("fullName")), "%" + search.toLowerCase() + "%");
                Predicate emailLike =
                        cb.like(cb.lower(root.get("email")), "%" + search.toLowerCase() + "%");
                predicates.add(cb.or(nameLike, emailLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
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
    public void restoreUser(Integer userId) {
        User userToRestore = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        if (!Boolean.TRUE.equals(userToRestore.getIsDeleted())) {
            throw new ValidationException("Người dùng này chưa bị xóa, không thể restore.");
        }

        userToRestore.setIsDeleted(false);
        userToRestore.setDeletedAt(null);
        userRepository.save(userToRestore);

        log.info("Đã restore user ID: {}", userId);
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

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId);
        }

        return createResponseWithAvatarUrl(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Integer userId, UpdateUserRequest request) {
        User userToUpdate = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        if (Boolean.TRUE.equals(userToUpdate.getIsDeleted())) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId);
        }

        if (request.fullName() != null) {
            userToUpdate.setFullName(request.fullName());
        }

        if (request.isAdmin() != null) {
            userToUpdate.setIsAdmin(request.isAdmin());
        }

        User updatedUser = userRepository.save(userToUpdate);

        return createResponseWithAvatarUrl(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Integer userId) {
        User userToDelete = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        if (Boolean.TRUE.equals(userToDelete.getIsDeleted())) {
            throw new ValidationException("Người dùng này đã bị xóa trước đó.");
        }

        // Soft delete: Đánh dấu user là đã xóa
        userToDelete.setIsDeleted(true);
        userToDelete.setDeletedAt(Instant.now());
        userRepository.save(userToDelete);

        // Xóa avatar file
        String oldObjectName = userToDelete.getAvatar();
        if (oldObjectName != null && !oldObjectName.isBlank()) {
            try {
                log.info("Đang cố gắng xóa avatar cho user đã bị xóa: {}", oldObjectName);
                fileStorageService.deleteFile(oldObjectName);
            } catch (Exception e) {
                log.error("Không thể xóa avatar '{}': {}", oldObjectName, e.getMessage());
            }
        }

        // Xóa refresh tokens để đăng xuất user khỏi tất cả thiết bị
        userRefreshTokenRepository.deleteByUserId(userId);

        // Xóa password reset tokens nếu có
        passwordResetTokenRepository.deleteByUserId(userId);

        // Xóa relationships (user roles, user permissions)
        // Lưu ý: Xóa relationships thông qua repositories riêng biệt nếu cần
        // Hiện tại để JPA cascade hoặc xử lý ở module khác

        log.info("Đã soft delete user ID: {}", userId);
    }

    @Override
    @Transactional
    public UserResponse uploadAvatar(String userEmail, MultipartFile file) {
        validateAvatarFile(file);

        User currentUser = findUserByEmail(userEmail);

        String oldObjectName = currentUser.getAvatar();
        if (oldObjectName != null && !oldObjectName.isBlank()) {
            try {
                log.info("Đang cố gắng xóa avatar cũ: {}", oldObjectName);
                fileStorageService.deleteFile(oldObjectName);
            } catch (Exception e) {
                log.error("Không thể xóa avatar cũ '{}': {}", oldObjectName, e.getMessage());
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

                // Tạo mật khẩu ngẫu nhiên (12 ký tự: chữ hoa, chữ thường, số, ký tự đặc biệt)
                String newPassword = generateRandomPassword();
                String encodedPassword = passwordEncoder.encode(newPassword);

                // Cập nhật mật khẩu
                user.setHashPassword(encodedPassword);
                userRepository.save(user);

                // Vô hiệu hóa tất cả refresh tokens cho user này
                userRefreshTokenRepository.deleteByUserId(userId);

                // Gửi email với mật khẩu mới
                try {
                    emailService.sendNewPasswordEmail(user.getEmail(), user.getFullName(),
                            newPassword);
                    log.info("Đã gửi email mật khẩu mới tới user ID: {} ({})", userId,
                            user.getEmail());
                } catch (Exception emailException) {
                    log.error("Đã reset mật khẩu cho user ID {} nhưng không thể gửi email: {}",
                            userId, emailException.getMessage());
                    // Vẫn tính là thành công vì mật khẩu đã được reset
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

        // Đảm bảo có ít nhất một ký tự từ mỗi tập hợp (chữ hoa, chữ thường, số, ký tự đặc biệt)
        password.append(uppercase.charAt(random.nextInt(uppercase.length())));
        password.append(lowercase.charAt(random.nextInt(lowercase.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));

        // Điền phần còn lại ngẫu nhiên (12 - 4 = 8 ký tự nữa)
        for (int i = 4; i < 12; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // Xáo trộn mật khẩu
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
