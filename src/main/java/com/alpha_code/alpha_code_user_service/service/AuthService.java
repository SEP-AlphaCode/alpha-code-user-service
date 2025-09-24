package com.alpha_code.alpha_code_user_service.service;

import com.alpha_code.alpha_code_user_service.dto.AccountDto;
import com.alpha_code.alpha_code_user_service.dto.LoginDto;
import com.alpha_code.alpha_code_user_service.dto.ResetPassworDto;
import jakarta.mail.MessagingException;

public interface AuthService {
    LoginDto.LoginResponse login(LoginDto.LoginRequest loginRequest);

    AccountDto register(LoginDto.RegisterRequest registerRequest);

    LoginDto.LoginResponse googleLogin(String request);

    boolean requestResetPassword(String email) throws MessagingException;

    boolean confirmResetPassword(ResetPassworDto dto);
}
