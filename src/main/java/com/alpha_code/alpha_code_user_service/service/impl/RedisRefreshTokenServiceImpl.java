package com.alpha_code.alpha_code_user_service.service.impl;

import com.alpha_code.alpha_code_user_service.service.RedisRefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisRefreshTokenServiceImpl implements RedisRefreshTokenService {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "refreshToken:";

    @Override
    public void save(UUID userId, String refreshToken, long duration, TimeUnit unit) {
        String key = PREFIX + userId;
        redisTemplate.opsForValue().set(key, refreshToken, duration, unit);
    }

    @Override
    public String get(UUID userId) {
        return redisTemplate.opsForValue().get(PREFIX + userId);
    }

    @Override
    public void delete(UUID userId) {
        redisTemplate.delete(PREFIX + userId);
    }

    @Override
    public boolean validate(UUID userId, String refreshToken) {
        String stored = get(userId);
        return stored != null && stored.equals(refreshToken);
    }
}