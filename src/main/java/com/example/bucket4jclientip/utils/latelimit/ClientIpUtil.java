package com.example.bucket4jclientip.utils.latelimit;

import jakarta.servlet.http.HttpServletRequest;

public class ClientIpUtil {
    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
    };

    /**
     * @param request current HTTP request
     * @return (String) client Ip
     * @apiNote 'X-Forwarded-For' 참고: https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-For
     */
    public static String getClientIp(HttpServletRequest request) {
        for (String header : IP_HEADER_CANDIDATES) {
            String ipAddress = request.getHeader(header);
            if (ipAddress != null && !ipAddress.isEmpty() && !"unknown".equalsIgnoreCase(ipAddress)) {
                // 여러 IP가 있을 경우 첫 번째 IP 반환
                return ipAddress.split(",")[0];
            }
        }
        // 유효한 헤더에서 IP를 찾지 못한 경우 원격 주소 반환
        return request.getRemoteAddr();
    }
}
