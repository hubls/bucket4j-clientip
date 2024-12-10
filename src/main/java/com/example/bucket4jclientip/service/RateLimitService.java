package com.example.bucket4jclientip.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.bucket4jclientip.utils.latelimit.RateLimitServiceConstants.*;

@Slf4j
@Service
public class RateLimitService {
    private Map<String, RequestInfo> rateLimitErrorCounter = new ConcurrentHashMap<>();

    public boolean isLimitReachedThreshold(String clientIp) {
        RequestInfo requestInfo = rateLimitErrorCounter.computeIfAbsent(clientIp,
                key -> new RequestInfo());

        if (!isApplyHandling(clientIp, requestInfo)) {
            requestInfo.startCount();
            return false;
        }
        return true;
    }

    private boolean isApplyHandling(String clientIp, RequestInfo requestInfo) {
        if (requestInfo.isWithinTimeWindow()) { // 1시간 이내에 같은 Client IP로 부터 요청이 있었다면.
            int count = requestInfo.incrementAndGetCount();

            log.info("[Rate limit count] client IP : {}  limit count  : {} ", clientIp, count);

            checkAndResetIfLimitExceeded(clientIp, requestInfo, count);
            return true;
        }
        return false;
    }

    private void checkAndResetIfLimitExceeded(String clientIp, RequestInfo requestInfo, int count) {
        if (count >= MAX_REQUEST_COUNT) {
            requestInfo.resetCount();

            log.info("Rate limit is occurred 10 or more times for this client IP: {}", clientIp);
        }
    }

    private static class RequestInfo {
        private AtomicInteger count = new AtomicInteger(0);
        private LocalDateTime lastRequestTime;

        private int incrementAndGetCount() {
            return this.count.incrementAndGet();
        }

        public boolean isWithinTimeWindow() {
            LocalDateTime now = LocalDateTime.now();
            if (lastRequestTime == null || ChronoUnit.HOURS.between(lastRequestTime, now) >= TIME_WINDOW_HOURS) {
                lastRequestTime = now;
                return false;
            }
            return true;
        }

        public void startCount() {
            count.set(1);
            lastRequestTime = LocalDateTime.now();
        }

        public void resetCount() {
            count.set(0);
            lastRequestTime = LocalDateTime.now();
        }
    }
}
