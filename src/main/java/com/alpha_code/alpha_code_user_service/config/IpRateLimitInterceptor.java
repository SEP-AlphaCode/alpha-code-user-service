package com.alpha_code.alpha_code_user_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class IpRateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;


    // config
    private static final int MAX_REQUESTS = 20;      // tối đa 20 request
    private static final int WINDOW_SECONDS = 60;    // trong 60 giây

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = extractClientIp(request);
        String key = "rate-limit:" + ip;

        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(WINDOW_SECONDS));
        }

        if (count != null && count > MAX_REQUESTS) {
            response.setStatus(429);
            response.setContentType("application/json");

            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("error", "Too Many Requests");
            body.put("status", 429);
            body.put("message", "Too many requests from IP: " + ip + ". Please try again later.");

            response.getWriter().write(objectMapper.writeValueAsString(body));
            return false;
        }

        return true;
    }

    private String extractClientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}

