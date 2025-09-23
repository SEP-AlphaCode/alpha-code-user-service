package com.alpha_code.alpha_code_user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequestDto implements Serializable {
    private String email;
}

