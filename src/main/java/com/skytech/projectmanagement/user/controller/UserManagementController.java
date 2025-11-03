package com.skytech.projectmanagement.user.controller;

import com.skytech.projectmanagement.common.dto.PaginatedResponse;
import com.skytech.projectmanagement.common.dto.SuccessResponse;
import com.skytech.projectmanagement.user.dto.CreateUserRequest;
import com.skytech.projectmanagement.user.dto.UpdateUserRequest;
import com.skytech.projectmanagement.user.dto.UserResponse;
import com.skytech.projectmanagement.user.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user-service/v1/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserService userService;

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<SuccessResponse<Object>> deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);

        SuccessResponse<Object> response = SuccessResponse.of(null, "Xóa người dùng thành công.");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('USER_READ')")
    public ResponseEntity<SuccessResponse<UserResponse>> getUserById(@PathVariable Integer userId) {
        UserResponse userDto = userService.getUserById(userId);

        SuccessResponse<UserResponse> response =
                SuccessResponse.of(userDto, "Lấy thông tin người dùng thành công.");

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<SuccessResponse<UserResponse>> updateUser(@PathVariable Integer userId,
            @Valid @RequestBody UpdateUserRequest request) {

        UserResponse updatedUserDto = userService.updateUser(userId, request);

        SuccessResponse<UserResponse> response =
                SuccessResponse.of(updatedUserDto, "Cập nhật thông tin người dùng thành công.");

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public ResponseEntity<SuccessResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        UserResponse newUserDto = userService.createUser(request);

        SuccessResponse<UserResponse> response =
                SuccessResponse.of(newUserDto, "Tạo người dùng thành công.");

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('USER_READ')")
    public ResponseEntity<PaginatedResponse<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) Boolean includeDeleted) {

        Sort sort = Sort.by(
                sortOrder.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy);

        Pageable pageable = PageRequest.of(page, size, sort);

        PaginatedResponse<UserResponse> paginatedData =
                userService.getUsers(pageable, search, includeDeleted);

        return ResponseEntity.ok(paginatedData);
    }

    @PatchMapping("/{userId}/restore")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<SuccessResponse<UserResponse>> restoreUser(@PathVariable Integer userId) {
        userService.restoreUser(userId);

        UserResponse restoredUser = userService.getUserById(userId);

        SuccessResponse<UserResponse> response =
                SuccessResponse.of(restoredUser, "Khôi phục người dùng thành công.");

        return ResponseEntity.ok(response);
    }
}
