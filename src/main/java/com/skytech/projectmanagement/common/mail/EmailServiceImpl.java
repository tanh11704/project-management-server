package com.skytech.projectmanagement.common.mail;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Yêu cầu đặt lại mật khẩu - Project Management");

            String htmlContent = buildPasswordResetEmailTemplate(token);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Đã gửi email reset password tới: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Lỗi khi gửi email tới {}: {}", toEmail, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendNewPasswordEmail(String toEmail, String userName, String newPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Mật khẩu mới - Project Management System");

            String htmlContent = buildNewPasswordEmailTemplate(
                    userName != null ? userName : "Người dùng", newPassword);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Đã gửi email mật khẩu mới tới: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Lỗi khi gửi email mật khẩu mới tới {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Không thể gửi email mật khẩu mới: " + e.getMessage(), e);
        }
    }

    private String buildPasswordResetEmailTemplate(String token) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Reset Mật khẩu</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f4f4f7;">
                    <table role="presentation" style="width: 100%; border-collapse: collapse;">
                        <tr>
                            <td style="padding: 40px 20px;">
                                <table role="presentation" style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);">
                                    <!-- Header -->
                                    <tr>
                                        <td style="padding: 40px 40px 30px; text-align: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 12px 12px 0 0;">
                                            <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: 600;">🔐 Đặt lại Mật khẩu</h1>
                                        </td>
                                    </tr>

                                    <!-- Content -->
                                    <tr>
                                        <td style="padding: 40px;">
                                            <p style="margin: 0 0 20px; color: #333333; font-size: 16px; line-height: 1.6;">
                                                Xin chào,
                                            </p>
                                            <p style="margin: 0 0 20px; color: #333333; font-size: 16px; line-height: 1.6;">
                                                Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản của mình. Bạn có thể sử dụng mật khẩu tạm thời bên dưới để đăng nhập:
                                            </p>

                                            <!-- Password Box -->
                                            <div style="background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); border-left: 4px solid #667eea; padding: 20px; margin: 30px 0; border-radius: 8px;">
                                                <p style="margin: 0 0 10px; color: #555555; font-size: 14px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;">Mật khẩu tạm thời:</p>
                                                <p style="margin: 0; color: #333333; font-size: 24px; font-weight: 700; font-family: 'Courier New', monospace; letter-spacing: 2px; word-break: break-all;">{TOKEN}</p>
                                            </div>

                                            <div style="background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; border-radius: 6px;">
                                                <p style="margin: 0; color: #856404; font-size: 14px; line-height: 1.5;">
                                                    ⏰ <strong>Lưu ý:</strong> Mật khẩu này sẽ hết hạn sau <strong>1 giờ</strong>. Vui lòng đăng nhập và thay đổi mật khẩu ngay sau khi đăng nhập.
                                                </p>
                                            </div>

                                            <p style="margin: 30px 0 20px; color: #666666; font-size: 14px; line-height: 1.6;">
                                                Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này hoặc liên hệ với quản trị viên nếu bạn có thắc mắc.
                                            </p>
                                        </td>
                                    </tr>

                                    <!-- Footer -->
                                    <tr>
                                        <td style="padding: 30px 40px; background-color: #f8f9fa; border-radius: 0 0 12px 12px; border-top: 1px solid #e9ecef;">
                                            <p style="margin: 0; color: #6c757d; font-size: 14px; text-align: center; line-height: 1.6;">
                                                Trân trọng,<br>
                                                <strong style="color: #667eea;">Hệ thống Quản lý Dự án</strong>
                                            </p>
                                            <p style="margin: 15px 0 0; color: #adb5bd; font-size: 12px; text-align: center;">
                                                Email này được gửi tự động, vui lòng không phản hồi.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """
                .replace("{TOKEN}", token);
    }

    private String buildNewPasswordEmailTemplate(String userName, String newPassword) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Mật khẩu Mới</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f4f4f7;">
                    <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                        <tr>
                            <td style="padding: 40px 20px;">
                                <table role="presentation" style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);">
                                    <!-- Header -->
                                    <tr>
                                        <td style="padding: 40px 40px 30px; text-align: center; background: linear-gradient(135deg, #11998e 0%%, #38ef7d 100%%); border-radius: 12px 12px 0 0;">
                                            <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: 600;">✨ Mật khẩu Mới</h1>
                                            <p style="margin: 15px 0 0; color: #ffffff; font-size: 16px; opacity: 0.95;">Tài khoản của bạn đã được reset mật khẩu</p>
                                        </td>
                                    </tr>

                                    <!-- Content -->
                                    <tr>
                                        <td style="padding: 40px;">
                                            <p style="margin: 0 0 20px; color: #333333; font-size: 18px; line-height: 1.6;">
                                                Xin chào <strong style="color: #11998e;">{USER_NAME}</strong>,
                                            </p>
                                            <p style="margin: 0 0 25px; color: #333333; font-size: 16px; line-height: 1.6;">
                                                Quản trị viên đã reset mật khẩu cho tài khoản của bạn. Dưới đây là mật khẩu mới của bạn:
                                            </p>

                                            <!-- Password Box -->
                                            <div style="background: linear-gradient(135deg, #11998e 0%%, #38ef7d 100%%); padding: 25px; margin: 30px 0; border-radius: 10px; text-align: center; box-shadow: 0 2px 8px rgba(17, 153, 142, 0.3);">
                                                <p style="margin: 0 0 10px; color: #ffffff; font-size: 13px; font-weight: 600; text-transform: uppercase; letter-spacing: 1px; opacity: 0.9;">Mật khẩu mới của bạn:</p>
                                                <p style="margin: 0; color: #ffffff; font-size: 28px; font-weight: 700; font-family: 'Courier New', monospace; letter-spacing: 3px; word-break: break-all; text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);">{PASSWORD}</p>
                                            </div>

                                            <!-- Security Notice -->
                                            <div style="background-color: #e3f2fd; border-left: 4px solid #2196f3; padding: 18px; margin: 25px 0; border-radius: 6px;">
                                                <p style="margin: 0; color: #1565c0; font-size: 14px; line-height: 1.6;">
                                                    🔒 <strong>Bảo mật:</strong> Vì lý do bảo mật, chúng tôi khuyến nghị bạn nên:
                                                </p>
                                                <ul style="margin: 10px 0 0 20px; color: #1565c0; font-size: 14px; line-height: 1.8;">
                                                    <li>Đăng nhập ngay với mật khẩu mới</li>
                                                    <li>Thay đổi mật khẩu thành một mật khẩu mạnh và dễ nhớ</li>
                                                    <li>Không chia sẻ mật khẩu với bất kỳ ai</li>
                                                </ul>
                                            </div>

                                            <!-- Call to Action -->
                                            <div style="text-align: center; margin: 35px 0 25px;">
                                                <a href="#" style="display: inline-block; padding: 14px 32px; background: linear-gradient(135deg, #11998e 0%%, #38ef7d 100%%); color: #ffffff; text-decoration: none; border-radius: 8px; font-weight: 600; font-size: 16px; box-shadow: 0 4px 6px rgba(17, 153, 142, 0.3);">
                                                    Đăng nhập ngay
                                                </a>
                                            </div>

                                            <p style="margin: 25px 0 0; color: #666666; font-size: 14px; line-height: 1.6;">
                                                Nếu bạn không yêu cầu reset mật khẩu, vui lòng liên hệ ngay với quản trị viên để được hỗ trợ.
                                            </p>
                                        </td>
                                    </tr>

                                    <!-- Footer -->
                                    <tr>
                                        <td style="padding: 30px 40px; background-color: #f8f9fa; border-radius: 0 0 12px 12px; border-top: 1px solid #e9ecef;">
                                            <p style="margin: 0; color: #6c757d; font-size: 14px; text-align: center; line-height: 1.6;">
                                                Trân trọng,<br>
                                                <strong style="color: #11998e; font-size: 16px;">Hệ thống Quản lý Dự án</strong>
                                            </p>
                                            <div style="margin-top: 20px; padding-top: 20px; border-top: 1px solid #e9ecef;">
                                                <p style="margin: 0; color: #adb5bd; font-size: 12px; text-align: center; line-height: 1.5;">
                                                    💌 Email này được gửi tự động từ hệ thống.<br>
                                                    Vui lòng không phản hồi email này. Nếu cần hỗ trợ, vui lòng liên hệ với quản trị viên.
                                                </p>
                                            </div>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """
                .replace("{USER_NAME}", userName != null ? userName : "Người dùng")
                .replace("{PASSWORD}", newPassword);
    }

}
