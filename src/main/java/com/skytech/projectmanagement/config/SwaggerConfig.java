package com.skytech.projectmanagement.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Cấu hình Swagger/OpenAPI cho dự án Project Management
 *
 * <p>
 * Swagger UI có thể truy cập tại: http://localhost:8080/swagger-ui.html
 * </p>
 * <p>
 * OpenAPI JSON: http://localhost:8080/v3/api-docs
 * </p>
 *
 * <p>
 * <b>Lưu ý:</b> Swagger UI chỉ nên được bật trong môi trường development. Trong production, nên tắt
 * hoặc bảo vệ bằng authentication.
 * </p>
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Project Management API").version("1.0.0")
                        .description(buildApiDescription())
                        .contact(new Contact().name("SkyTech Development Team")
                                .email("support@skytech.com").url("https://skytech.com"))
                        .license(new License().name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(buildServers())
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components().addSecuritySchemes("Bearer Authentication",
                        buildSecurityScheme()));
    }

    private String buildApiDescription() {
        return """
                # 📚 API Documentation - Hệ Thống Quản Lý Dự Án

                Đây là tài liệu API đầy đủ và chi tiết cho hệ thống quản lý dự án. API này cung cấp các chức năng quản lý dự án, người dùng, tasks, bugs và nhiều tính năng khác.

                ## 🎯 Tổng Quan

                Hệ thống Project Management API được xây dựng dựa trên:
                - **Framework**: Spring Boot 3.5.7
                - **Language**: Java 17
                - **Database**: PostgreSQL
                - **Authentication**: JWT (JSON Web Token)
                - **File Storage**: Cloudinary

                ## 📋 Các Module Chính

                ### 🔐 Xác Thực & Phân Quyền (`/auth-service/v1`)
                - **Đăng nhập/Đăng xuất**: Quản lý phiên đăng nhập của người dùng
                - **Token Management**: Làm mới token, quản lý refresh token
                - **Quên mật khẩu**: Yêu cầu đặt lại mật khẩu qua email
                - **Quản lý Roles**: Tạo, cập nhật, xóa roles
                - **Quản lý Permissions**: Phân quyền chi tiết cho từng chức năng
                - **Gán Roles cho Users**: Quản lý quyền hạn của người dùng

                ### 👥 Quản Lý Người Dùng (`/user-service/v1`)
                - **CRUD Người Dùng**: Tạo, đọc, cập nhật, xóa người dùng
                - **Quản Lý Profile**: Xem và cập nhật thông tin cá nhân
                - **Upload Avatar**: Tải lên và quản lý ảnh đại diện
                - **Đổi Mật Khẩu**: Thay đổi mật khẩu cá nhân
                - **Soft Delete**: Xóa mềm và khôi phục người dùng

                ### 📁 Quản Lý Dự Án (`/project-service/v1/projects`)
                - **Tạo Dự Án**: Khởi tạo dự án mới với các thông tin cơ bản
                - **Quản Lý Thành Viên**: Thêm, xóa, cập nhật vai trò thành viên
                - **Import Teams**: Nhập team vào dự án
                - **Xem Chi Tiết**: Lấy thông tin đầy đủ về dự án và thành viên
                - **Phân Quyền**: Quản lý quyền hạn của từng thành viên

                ### ✅ Quản Lý Tasks (`/task-service/v1/tasks`)
                - **Tạo Task**: Tạo công việc mới trong dự án
                - **Cập Nhật Task**: Sửa đổi thông tin và trạng thái task
                - **Gán Người Thực Hiện**: Phân công task cho thành viên
                - **Lọc & Tìm Kiếm**: Tìm kiếm task theo nhiều tiêu chí
                - **Quản Lý Trạng Thái**: Cập nhật tiến độ và trạng thái task

                ### 🐛 Quản Lý Bugs (`/bug-service/v1/bugs`)
                - **Báo Cáo Bug**: Tạo báo cáo lỗi mới
                - **Cập Nhật Bug**: Sửa đổi thông tin bug
                - **Gán Người Xử Lý**: Phân công bug cho developer
                - **Theo Dõi Bug**: Quản lý trạng thái và tiến độ xử lý
                - **Lọc Theo Dự Án**: Xem danh sách bugs của dự án

                ### 👨‍👩‍👧‍👦 Quản Lý Teams (`/teams-service/v1/teams`)
                - **Tạo Team**: Tạo nhóm làm việc mới
                - **Quản Lý Thành Viên**: Thêm, xóa thành viên trong team
                - **Cập Nhật Team**: Sửa đổi thông tin team

                ### 💬 Comments (`/comment-service/v1/comments`)
                - **Thêm Comment**: Bình luận trên task hoặc bug
                - **Xem Comments**: Lấy danh sách comments
                - **Cập Nhật Comment**: Sửa đổi nội dung comment

                ### 🔔 Notifications (`/notification-service/v1/notifications`)
                - **Xem Thông Báo**: Lấy danh sách thông báo
                - **Đánh Dấu Đã Đọc**: Cập nhật trạng thái đã đọc
                - **WebSocket**: Nhận thông báo real-time

                ## 🔑 Xác Thực JWT

                Hầu hết các API yêu cầu xác thực bằng JWT Bearer Token:

                1. **Đăng nhập** để lấy token:
                   ```
                   POST /auth-service/v1/login
                   {
                     "email": "user@example.com",
                     "password": "your-password"
                   }
                   ```

                2. **Sử dụng token** trong các request tiếp theo:
                   ```
                   Authorization: Bearer {your-access-token}
                   ```

                3. **Làm mới token** khi token sắp hết hạn:
                   ```
                   POST /auth-service/v1/refresh
                   {
                     "refresh_token": "your-refresh-token"
                   }
                   ```

                ### Thông Tin Token
                - **Access Token**: Thời gian hết hạn: 15 phút (có thể cấu hình)
                - **Refresh Token**: Thời gian hết hạn: 7 ngày
                - **Token Format**: JWT (JSON Web Token)
                - **Algorithm**: HS256

                ## 📝 Quy Tắc Đặt Tên

                - **Request Body**: Sử dụng snake_case (ví dụ: `full_name`, `is_admin`)
                - **Response**: Sử dụng snake_case
                - **Path Variables**: camelCase (ví dụ: `userId`, `projectId`)
                - **Query Parameters**: snake_case (ví dụ: `page`, `size`, `sort_by`)

                ## 🛡️ Phân Quyền

                Hệ thống sử dụng Role-Based Access Control (RBAC) với các permission chi tiết:

                - **USER_READ**: Xem thông tin người dùng
                - **USER_CREATE**: Tạo người dùng mới
                - **USER_UPDATE**: Cập nhật thông tin người dùng
                - **USER_DELETE**: Xóa người dùng
                - **PROJECT_MANAGE_ANY**: Quản lý tất cả dự án
                - **PROJECT_MEMBER_MANAGE**: Quản lý thành viên dự án
                - **TASK_CREATE**: Tạo task
                - **TASK_UPDATE**: Cập nhật task
                - **BUG_CREATE**: Tạo bug
                - **BUG_UPDATE**: Cập nhật bug
                - Và nhiều permissions khác...

                ## ⚠️ Lưu Ý Quan Trọng

                1. **Mật Khẩu**: Mật khẩu phải đáp ứng yêu cầu:
                   - Ít nhất 8 ký tự
                   - Có chữ hoa, chữ thường
                   - Có số
                   - Có ký tự đặc biệt

                2. **File Upload**:
                   - Kích thước tối đa: 10MB
                   - Định dạng hình ảnh: JPG, JPEG, PNG, GIF
                   - Files được lưu trữ trên Cloudinary

                3. **Rate Limiting**: Một số API có giới hạn số lượng request để tránh spam

                4. **Error Handling**: Tất cả lỗi được trả về theo format chuẩn:
                   ```json
                   {
                     "success": false,
                     "message": "Mô tả lỗi",
                     "data": null
                   }
                   ```

                5. **Pagination**: Các API danh sách hỗ trợ phân trang:
                   - `page`: Số trang (bắt đầu từ 0)
                   - `size`: Số lượng item mỗi trang
                   - `sort_by`: Trường sắp xếp
                   - `sort_order`: Thứ tự sắp xếp (asc/desc)

                ## 📞 Hỗ Trợ

                Nếu bạn gặp vấn đề hoặc có câu hỏi:
                - **Email**: support@skytech.com
                - **Documentation**: Xem thêm trong file SWAGGER-GUIDE.md
                - **Issues**: Báo cáo lỗi qua hệ thống quản lý issues

                ## 📄 License

                API này được phát hành dưới giấy phép MIT License.
                """;
    }

    /**
     * Xây dựng danh sách servers
     */
    private List<Server> buildServers() {
        return List.of(
                new Server().url("http://localhost:8080").description(
                        "🏠 Môi trường Development - Sử dụng cho phát triển và testing"),
                new Server().url("https://staging-api.projectmanagement.com").description(
                        "🧪 Môi trường Staging - Sử dụng cho testing trước khi deploy"),
                new Server().url("https://api.projectmanagement.com")
                        .description("🚀 Môi trường Production - Môi trường chính thức"));
    }

    /**
     * Xây dựng cấu hình security scheme cho JWT
     */
    private SecurityScheme buildSecurityScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer")
                .bearerFormat("JWT").description("""
                        ## 🔐 Xác Thực JWT Bearer Token

                        ### Cách Sử Dụng:

                        1. **Lấy Token**:
                           - Đăng nhập qua endpoint `/auth-service/v1/login`
                           - Nhận `access_token` và `refresh_token` từ response

                        2. **Sử dụng Token**:
                           - Thêm vào header của mỗi request:
                             ```
                             Authorization: Bearer {your-access-token}
                             ```
                           - Hoặc sử dụng nút "Authorize" ở góc trên bên phải trong Swagger UI

                        3. **Làm Mới Token**:
                           - Khi `access_token` sắp hết hạn, sử dụng `refresh_token`:
                             ```
                             POST /auth-service/v1/refresh
                             {
                               "refresh_token": "your-refresh-token"
                             }
                             ```

                        ### Thông Tin Token:

                        - **Access Token**:
                          - Thời gian hết hạn: 15 phút (có thể cấu hình trong `application.yml`)
                          - Chứa thông tin: email, roles, permissions
                          - Sử dụng cho tất cả các API yêu cầu authentication

                        - **Refresh Token**:
                          - Thời gian hết hạn: 7 ngày
                          - Dùng để lấy `access_token` mới
                          - Lưu trữ trong database với IP address

                        ### Bảo Mật:

                        - Token được mã hóa bằng thuật toán HS256
                        - Token sẽ bị vô hiệu hóa sau khi đăng xuất (thêm vào blacklist)
                        - Không chia sẻ token với người khác
                        - Refresh token được rotate sau mỗi lần sử dụng

                        ### Lưu Ý:

                        - Token sẽ tự động hết hạn sau thời gian quy định
                        - Nếu token hết hạn, bạn cần đăng nhập lại hoặc sử dụng refresh token
                        - Mỗi lần đăng xuất, token sẽ bị vô hiệu hóa ngay lập tức
                        """);
    }
}
