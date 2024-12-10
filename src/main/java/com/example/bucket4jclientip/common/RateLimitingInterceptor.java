package com.example.bucket4jclientip.common;

import com.example.bucket4jclientip.service.RateLimitService;
import com.example.bucket4jclientip.utils.latelimit.ClientIpUtil;
import com.example.bucket4jclientip.utils.latelimit.RateLimitRefillChecker;
import com.example.bucket4jclientip.utils.latelimit.RateLimitResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.bucket4jclientip.utils.latelimit.RateLimitBucketConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitingInterceptor implements HandlerInterceptor {
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    private final RateLimitService rateLimitService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String clientIp = ClientIpUtil.getClientIp(request);

        Bucket bucket = cache.computeIfAbsent(clientIp, key -> newBucket());
        ConsumptionProbe consumptionProbe = bucket.tryConsumeAndReturnRemaining(REQUEST_COST_IN_TOKENS);

        if (isRateLimitExceeded(request, response, clientIp, consumptionProbe)) {
            return false;
        }

        return true;
    }

    /**
     * @apiNote rate limit 발생여부에 따른 각각의 success, error response 를 생성 및 반환 합니다. *
     * 'rateLimitService.isLimitReachedThreshold(...)' 특정 IP에 대한 rate limit 허용치 초과 여부를 check 합니다.*
     */
    private boolean isRateLimitExceeded(HttpServletRequest request, HttpServletResponse response,
                                        String clientIp, ConsumptionProbe consumptionProbe) {

        // 초과 요청을 한 사용자 처리
        if (!consumptionProbe.isConsumed()) {
            float waitForRefill = RateLimitRefillChecker.getRoundedSecondsToWaitForRefill(consumptionProbe);

            RateLimitResponse.errorResponse(response, BUCKET_CAPACITY, CALLS_IN_SECONDS, waitForRefill);

            log.warn(
                    "rate limit exceeded for client IP :{}  Refill in {} seconds  Request "
                            + "details: method = {} URI = {}",
                    clientIp, waitForRefill, request.getMethod(), request.getRequestURI());

            rateLimitService.isLimitReachedThreshold(clientIp);
            return true;
        }

        RateLimitResponse.successResponse(response, consumptionProbe.getRemainingTokens(), BUCKET_CAPACITY, CALLS_IN_SECONDS);
        log.info("[{}] remaining token: {}", clientIp, consumptionProbe.getRemainingTokens());
        return false;
    }

    private Bucket newBucket() {
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(BUCKET_CAPACITY)
                .refillIntervally(BUCKET_TOKENS, CALLS_IN_SECONDS)
                .build();

        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }
}
