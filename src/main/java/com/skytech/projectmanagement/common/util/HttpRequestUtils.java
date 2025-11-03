package com.skytech.projectmanagement.common.util;

import org.springframework.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;

public class HttpRequestUtils {

    /**
     * Lấy địa chỉ IP thực của client từ HttpServletRequest Xử lý các trường hợp đứng sau proxy/load
     * balancer (X-Forwarded-For, X-Real-IP)
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = null;

        // Kiểm tra X-Forwarded-For header (phổ biến nhất khi đứng sau proxy)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor) && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            // X-Forwarded-For có thể chứa nhiều IP, lấy IP đầu tiên
            ipAddress = xForwardedFor.split(",")[0].trim();
        }

        // Nếu chưa có, kiểm tra X-Real-IP header
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }

        // Nếu vẫn chưa có, kiểm tra Proxy-Client-IP
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }

        // Nếu vẫn chưa có, kiểm tra WL-Proxy-Client-IP (WebLogic)
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }

        // Nếu vẫn chưa có, kiểm tra HTTP_CLIENT_IP
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }

        // Cuối cùng, lấy IP trực tiếp từ request
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        return ipAddress != null ? ipAddress : "unknown";
    }

    /**
     * Tạo deviceInfo tự động từ IP và User-Agent nếu frontend không gửi
     */
    public static String generateDeviceInfo(HttpServletRequest request, String providedDeviceInfo) {
        if (StringUtils.hasText(providedDeviceInfo)) {
            return providedDeviceInfo;
        }

        String userAgent = request.getHeader("User-Agent");
        String ipAddress = getClientIpAddress(request);

        // Tạo deviceInfo từ User-Agent và IP
        String deviceInfo = "IP: " + ipAddress;
        if (StringUtils.hasText(userAgent)) {
            // Lấy thông tin cơ bản từ User-Agent (có thể mở rộng thêm)
            if (userAgent.contains("Mobile") || userAgent.contains("Android")
                    || userAgent.contains("iPhone")) {
                deviceInfo += " | Mobile";
            } else {
                deviceInfo += " | Desktop";
            }
        }

        return deviceInfo;
    }
}

