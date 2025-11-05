package com.skytech.projectmanagement.user.controller;

import com.skytech.projectmanagement.common.dto.SuccessResponse;
import com.skytech.projectmanagement.user.dto.ChangePasswordRequest;
import com.skytech.projectmanagement.user.dto.UserProfileResponse;
import com.skytech.projectmanagement.user.dto.UserResponse;
import com.skytech.projectmanagement.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user-service/v1/me")
@RequiredArgsConstructor
@Tag(name = "Profile Người Dùng", description = """
        API quản lý thông tin profile của người dùng hiện tại, bao gồm:
        - Xem thông tin profile và permissions
        - Upload avatar
        - Đổi mật khẩu

        **Lưu ý:** Tất cả các API trong controller này yêu cầu authentication.
        """)
@SecurityRequirement(name = "Bearer Authentication")
public class UserProfileController {

    private final UserService userService;

    @Operation(summary = "Upload avatar", description = """
            Endpoint này cho phép người dùng upload hoặc cập nhật avatar của mình.

            **Lưu ý:**
            - File phải là hình ảnh (jpg, jpeg, png, gif)
            - Kích thước tối đa: 10MB
            - File sẽ được upload lên Cloudinary và trả về URL
            - Avatar cũ (nếu có) sẽ được thay thế
            """, tags = {"Profile Người Dùng"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload avatar thành công",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "File không hợp lệ hoặc quá lớn",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập", content = @Content)})
    @PostMapping("/avatar")
    public ResponseEntity<SuccessResponse<UserResponse>> uploadAvatar(
            @RequestParam("avatar") MultipartFile file) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();

        UserResponse updatedUserDto = userService.uploadAvatar(userEmail, file);

        SuccessResponse<UserResponse> response =
                SuccessResponse.of(updatedUserDto, "Cập nhật avatar thành công.");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lấy thông tin profile của người dùng hiện tại", description = """
            Endpoint này trả về thông tin đầy đủ của người dùng hiện tại, bao gồm:
            - Thông tin cơ bản: id, tên, email, avatar, ngày tạo
            - Cây phân quyền (permission tree) của người dùng

            **Lưu ý:**
            - Permission tree được xây dựng dựa trên roles và permissions của user
            - Nếu user là admin, sẽ có tất cả permissions
            - Permission tree được filter để chỉ hiển thị các permissions mà user có
            """, tags = {"Profile Người Dùng"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin profile thành công",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy thông tin người dùng",
                    content = @Content)})
    @GetMapping
    public ResponseEntity<SuccessResponse<UserProfileResponse>> getMyProfile() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();

        UserProfileResponse userProfileDto = userService.getUserProfileWithPermissions(userEmail);

        SuccessResponse<UserProfileResponse> response =
                SuccessResponse.of(userProfileDto, "Lấy thông tin người dùng thành công.");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Đổi mật khẩu",
            description = """
                    Endpoint này cho phép người dùng đổi mật khẩu của mình.

                    **Yêu cầu:**
                    - `old_password`: Mật khẩu hiện tại (hoặc mật khẩu tạm thời nếu đã nhận từ email)
                    - `new_password`: Mật khẩu mới (phải đáp ứng yêu cầu: ít nhất 8 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt)

                    **Lưu ý:**
                    - Mật khẩu mới không được trùng với mật khẩu cũ
                    - Có thể sử dụng mật khẩu tạm thời (từ email quên mật khẩu) để đổi mật khẩu
                    - Sau khi đổi mật khẩu thành công, tất cả refresh tokens sẽ bị xóa (yêu cầu đăng nhập lại)
                    """,
            tags = {"Profile Người Dùng"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đổi mật khẩu thành công",
                    content = @Content),
            @ApiResponse(responseCode = "400",
                    description = "Mật khẩu cũ không đúng hoặc mật khẩu mới không đáp ứng yêu cầu",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập", content = @Content)})
    @PostMapping("/change-password")
    public ResponseEntity<SuccessResponse<Object>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        userService.changePassword(auth.getName(), request);

        return ResponseEntity.ok(SuccessResponse.of(null, "Thay đổi mật khẩu thành công."));
    }
}
