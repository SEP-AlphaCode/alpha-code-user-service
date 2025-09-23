package com.alpha_code.alpha_code_user_service.controller;

import com.alpha_code.alpha_code_user_service.dto.LoginDto;
import com.alpha_code.alpha_code_user_service.dto.ResetPassworDto;
import com.alpha_code.alpha_code_user_service.dto.ResetPasswordRequestDto;
import com.alpha_code.alpha_code_user_service.service.AuthService;
import com.alpha_code.alpha_code_user_service.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth")
public class AuthController {

    private final AuthService service;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/reset-password/request")
    @Operation(summary = "Request reset password")
    public ResponseEntity<String> requestResetPassword(@RequestBody ResetPasswordRequestDto request) throws MessagingException {
        System.out.println("Incoming email = " + request.getEmail());
        boolean success = service.requestResetPassword(request.getEmail());
        System.out.println("Result from service = " + success);
        return success ? ResponseEntity.ok("Reset password link sent to email")
                : ResponseEntity.badRequest().body("Email not found or failed to send mail");
    }

    @PostMapping("/reset-password/reset")
    @Operation(summary = "Reset the password")
    public ResponseEntity<String> confirmResetPassword(@RequestBody ResetPassworDto dto) {
        boolean success = service.confirmResetPassword(dto);
        return success ? ResponseEntity.ok("Password reset successful")
                : ResponseEntity.badRequest().body("Token is invalid or expired");
    }

    @PostMapping("/login")
    @Operation(summary = "Login with username and password")
    public LoginDto.LoginResponse login(@RequestBody LoginDto.LoginRequest loginRequest) {
        return service.login(loginRequest);
    }

    @PostMapping("/refresh-new-token")
    @Operation(summary = "Refresh new access token using refresh token")
    public LoginDto.LoginResponse refreshNewToken(@RequestBody String refreshToken) {
        return refreshTokenService.refreshNewToken(refreshToken);
    }

    @PostMapping("/google-login")
    @Operation(summary = "Login with Google ID token")
    public LoginDto.LoginResponse googleLogin(@RequestBody String idToken) {
        return service.googleLogin(idToken);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and invalidate the refresh token")
    public String logout(@RequestBody String refreshToken) {
        return refreshTokenService.logout(refreshToken);
    }
}
