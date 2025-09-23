package com.alpha_code.alpha_code_user_service.service.impl;

import com.alpha_code.alpha_code_user_service.dto.LoginDto;
import com.alpha_code.alpha_code_user_service.entity.Account;
import com.alpha_code.alpha_code_user_service.exception.AuthenticationException;
import com.alpha_code.alpha_code_user_service.repository.AccountRepository;
import com.alpha_code.alpha_code_user_service.service.DashboardService;
import com.alpha_code.alpha_code_user_service.service.RedisRefreshTokenService;
import com.alpha_code.alpha_code_user_service.service.RefreshTokenService;
import com.alpha_code.alpha_code_user_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RedisRefreshTokenService redisService;
    private final JwtUtil jwtUtil;
    private final AccountRepository accountRepository;
    private final DashboardService dashboardService;

    @Value("${jwt.refresh-expiration-ms}")
    private Long refreshTokenDurationMs;

    @Override
    public String createRefreshToken(UUID userId, String refreshToken) {
        // Lưu refresh token vào Redis với TTL
        redisService.save(userId, refreshToken, refreshTokenDurationMs, TimeUnit.MILLISECONDS);
        return refreshToken;
    }

    @Override
    public boolean validateRefreshToken(UUID userId, String refreshToken) {
        return redisService.validate(userId, refreshToken);
    }

    @Override
    public void deleteRefreshToken(UUID userId) {
        redisService.delete(userId);
    }

    @Override
    public String getRefreshToken(UUID userId) {
        return redisService.get(userId);
    }

    @Override
    public LoginDto.LoginResponse refreshNewToken(String refreshToken) {
        UUID userId;
        try {
            userId = jwtUtil.getUserIdFromToken(refreshToken);
        } catch (Exception e) {
            throw new AuthenticationException("Invalid refresh token");
        }

        boolean valid = redisService.validate(userId, refreshToken);
        if (!valid) {
            throw new AuthenticationException("Refresh token is invalid or expired");
        }

        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        // cấp lại access token mới, refresh token giữ nguyên
        String newAccessToken = jwtUtil.generateAccessToken(account);

        return LoginDto.LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public String logout(String refreshToken) {
        UUID userId;
        try {
            userId = jwtUtil.getUserIdFromToken(refreshToken);
        } catch (Exception e) {
            throw new AuthenticationException("Invalid refresh token");
        }

        redisService.delete(userId);
        dashboardService.removeOnlineUser(userId);
        return "Logged out successfully";
    }
}