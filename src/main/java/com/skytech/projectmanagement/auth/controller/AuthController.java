package com.skytech.projectmanagement.auth.controller;

import com.skytech.projectmanagement.auth.dto.ForgotPasswordRequest;
import com.skytech.projectmanagement.auth.dto.LoginRequest;
import com.skytech.projectmanagement.auth.dto.LoginResponse;
import com.skytech.projectmanagement.auth.dto.LogoutRequest;
import com.skytech.projectmanagement.auth.dto.RefreshTokenRequest;
import com.skytech.projectmanagement.auth.dto.RefreshTokenResponse;
import com.skytech.projectmanagement.auth.service.AuthService;
import com.skytech.projectmanagement.common.dto.SuccessResponse;
import com.skytech.projectmanagement.common.util.HttpRequestUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller xử lý các API liên quan đến xác thực và quản lý phiên đăng nhập
 */
@RestController
@RequestMapping("/auth-service/v1")
@RequiredArgsConstructor
@Tag(name = "Xác Thực", description = """
        API quản lý xác thực người dùng, bao gồm:
        - Đăng nhập/Đăng xuất
        - Làm mới token
        - Quên mật khẩu
        - Đặt lại mật khẩu
        """)
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Đăng nhập hệ thống",
            description = """
                    Endpoint này cho phép người dùng đăng nhập vào hệ thống bằng email và mật khẩu.

                    **Lưu ý:**
                    - Email và mật khẩu là bắt buộc
                    - Mật khẩu phải đáp ứng yêu cầu: ít nhất 8 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt
                    - Sau khi đăng nhập thành công, bạn sẽ nhận được:
                      - `access_token`: Token để xác thực các request tiếp theo (thời gian hết hạn: 15 phút)
                      - `refresh_token`: Token để làm mới access token (thời gian hết hạn: 7 ngày)
                    - IP address được tự động lấy từ request, không cần gửi từ client
                    """,
            tags = {"Xác Thực"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đăng nhập thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "data": {
                                                    "user": {
                                                        "id": 1,
                                                        "full_name": "Nguyễn Văn A",
                                                        "email": "user@example.com",
                                                        "avatar": "https://example.com/avatar.jpg"
                                                    },
                                                    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                    "expires_in": 900
                                                },
                                                "message": "Đăng nhập thành công.",
                                                "success": true
                                            }
                                            """))),
            @ApiResponse(responseCode = "401", description = "Email hoặc mật khẩu không đúng",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ",
                    content = @Content)})
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest, HttpServletRequest httpRequest) {
        // Lấy IP address từ request (tự động)
        String ipAddress = HttpRequestUtils.getClientIpAddress(httpRequest);

        LoginResponse loginData = authService.login(loginRequest, ipAddress);

        SuccessResponse<LoginResponse> response =
                SuccessResponse.of(loginData, "Đăng nhập thành công.");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Đăng xuất khỏi hệ thống", description = """
            Endpoint này cho phép người dùng đăng xuất khỏi hệ thống.

            **Lưu ý:**
            - Cần cung cấp cả `refreshToken` và `accessToken`
            - Sau khi đăng xuất, cả hai token sẽ bị vô hiệu hóa
            - Access token sẽ được thêm vào blacklist và không thể sử dụng lại
            - Refresh token sẽ bị xóa khỏi database
            """, tags = {"Xác Thực"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đăng xuất thành công",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Token không hợp lệ hoặc đã hết hạn",
                    content = @Content)})
    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<Object>> logout(
            @Valid @RequestBody LogoutRequest logoutRequest) {
        authService.logout(logoutRequest.refreshToken(), logoutRequest.accessToken());

        SuccessResponse<Object> response = SuccessResponse.of(null, "Đăng xuất thành công.");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Làm mới access token", description = """
            Endpoint này cho phép làm mới access token khi token hiện tại sắp hết hạn.

            **Lưu ý:**
            - Cần cung cấp `refreshToken` hợp lệ
            - Sau khi làm mới, bạn sẽ nhận được:
              - `accessToken` mới (thời gian hết hạn: 15 phút)
              - `refreshToken` mới (thời gian hết hạn: 7 ngày)
            - Refresh token cũ sẽ bị vô hiệu hóa sau khi sử dụng
            - Nếu refresh token đã hết hạn, bạn cần đăng nhập lại
            """, tags = {"Xác Thực"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Làm mới token thành công",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Refresh token không hợp lệ",
                    content = @Content),
            @ApiResponse(responseCode = "401",
                    description = "Refresh token đã hết hạn hoặc không tồn tại",
                    content = @Content)})
    @PostMapping("/refresh")
    public ResponseEntity<SuccessResponse<RefreshTokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse responseData = authService.refreshToken(request);

        SuccessResponse<RefreshTokenResponse> response =
                SuccessResponse.of(responseData, "Làm mới token thành công.");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Quên mật khẩu", description = """
            Endpoint này cho phép người dùng yêu cầu đặt lại mật khẩu khi quên.

            **Quy trình:**
            1. Người dùng nhập email
            2. Hệ thống kiểm tra email có tồn tại không
            3. Nếu tồn tại, hệ thống sẽ:
               - Tạo một mật khẩu tạm thời
               - Gửi email chứa mật khẩu tạm thời đến người dùng
               - Mật khẩu tạm thời có thời gian hết hạn (thường là 24 giờ)

            **Lưu ý:**
            - API này luôn trả về thành công (200) để tránh leak thông tin về email tồn tại
            - Email sẽ được gửi bất đồng bộ để tăng tốc độ phản hồi
            - Mật khẩu tạm thời có thể được sử dụng để đăng nhập và đổi mật khẩu
            """, tags = {"Xác Thực"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = """
            Yêu cầu đã được xử lý. Nếu email tồn tại, người dùng sẽ nhận được email hướng dẫn.
            """, content = @Content),
            @ApiResponse(responseCode = "400", description = "Email không đúng định dạng",
                    content = @Content)})
    @PostMapping("/forgot-password")
    public ResponseEntity<SuccessResponse<Object>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.handleForgotPassword(request.email());

        SuccessResponse<Object> response = SuccessResponse.of(null,
                "Nếu email của bạn tồn tại trong hệ thống, bạn sẽ nhận được một hướng dẫn đặt lại mật khẩu.");

        return ResponseEntity.ok(response);
    }

}
