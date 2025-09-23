package com.alpha_code.alpha_code_user_service.service;

import com.alpha_code.alpha_code_user_service.dto.LoginDto;

import java.util.UUID;

public interface RefreshTokenService {
    String createRefreshToken(UUID userId, String refreshToken);
    boolean validateRefreshToken(UUID userId, String refreshToken);
    void deleteRefreshToken(UUID userId);
    String getRefreshToken(UUID userId);
    LoginDto.LoginResponse refreshNewToken(String refreshToken);
    String logout(String refreshToken);
}
