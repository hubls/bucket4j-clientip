package com.example.bucket4jclientip.utils.latelimit;

import com.example.bucket4jclientip.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;

import java.time.Duration;

public class RateLimitResponse {
    /**
     * @apiNote 'X-RateLimit-RetryAfter', 'X-RateLimit-Limit', 'X-RateLimit-Remaining' 참고:
     * https://sendbird.com/docs/chat/v3/platform-api/application/understanding-rate-limits/rate-limits
     * @apiNote `Retry-After` 참고:
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Retry-After
     */
    public static void successResponse(HttpServletResponse response,
                                       long remainingTokens, Long bucketCapacity, Duration callsInSeconds) {

        // 이 헤더는 클라이언트에게 얼마나 더 API 요청을 할 수 있는지에 대한 정보를 제공합니다.
        response.setHeader("X-RateLimit-Remaining", Long.toString(remainingTokens));
        // 이 헤더는 Rate Limiting 정책의 제한을 정의하며, 특정 시간 간격 내에서 허용되는 최대 요청 횟수를 알려줍니다.
        response.setHeader("X-RateLimit-Limit", bucketCapacity + ";w=" + callsInSeconds.getSeconds());
    }

    public static void errorResponse(HttpServletResponse response,
                                     Long bucketCapacity, Duration callsInSeconds, float waitForRefill) {

        response.setHeader("X-RateLimit-RetryAfter",Float.toString(waitForRefill));
        response.setHeader("X-RateLimit-Limit", bucketCapacity + ";w=" + callsInSeconds.getSeconds());
        response.setStatus(ErrorCode.TOO_MANY_REQUESTS.getHttpStatus().value());
    }

}
