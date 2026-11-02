package com.agriself.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory sliding-window rate limiting filter.
 * Protects against credential brute-forcing on /login and excessive API calls.
 */
@Component
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final int maxLoginAttempts;
    private final int maxRequestsPerMinute;

    private final Map<String, RequestBucket> loginAttempts = new ConcurrentHashMap<>();
    private final Map<String, RequestBucket> generalRequests = new ConcurrentHashMap<>();

    public RateLimitingFilter(
            @Value("${agriself.rate-limit.login-attempts-per-minute:10}") int maxLoginAttempts,
            @Value("${agriself.rate-limit.requests-per-minute:60}") int maxRequestsPerMinute) {
        this.maxLoginAttempts = maxLoginAttempts;
        this.maxRequestsPerMinute = maxRequestsPerMinute;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIp(request);
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 1. Rate-limit login POST attempts
        if ("/login".equals(uri) && "POST".equalsIgnoreCase(method)) {
            RequestBucket bucket = loginAttempts.computeIfAbsent(clientIp, k -> new RequestBucket());
            if (!bucket.allow(maxLoginAttempts, 60_000L)) {
                log.warn("Rate limit exceeded for login attempts from IP: {}", clientIp);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write("<div style='font-family:sans-serif;text-align:center;padding:50px;'>"
                        + "<h2>⚠️ Too Many Login Attempts</h2>"
                        + "<p>For security, please wait 1 minute before trying again.</p>"
                        + "<a href='/login'>Return to Login</a>"
                        + "</div>");
                return;
            }
        }

        // 2. Rate-limit general API requests
        if (uri.startsWith("/api/")) {
            RequestBucket bucket = generalRequests.computeIfAbsent(clientIp, k -> new RequestBucket());
            if (!bucket.allow(maxRequestsPerMinute, 60_000L)) {
                log.warn("Rate limit exceeded for API from IP: {}", clientIp);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Too many requests. Please slow down.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    private static class RequestBucket {
        private long windowStartTime = System.currentTimeMillis();
        private final AtomicInteger count = new AtomicInteger(0);

        synchronized boolean allow(int maxLimit, long windowDurationMs) {
            long now = System.currentTimeMillis();
            if (now - windowStartTime > windowDurationMs) {
                windowStartTime = now;
                count.set(0);
            }
            return count.incrementAndGet() <= maxLimit;
        }
    }
}
